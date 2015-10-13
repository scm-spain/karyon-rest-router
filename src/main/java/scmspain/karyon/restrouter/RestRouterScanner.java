package scmspain.karyon.restrouter;


import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.apache.commons.configuration.AbstractConfiguration;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.CustomSerialization;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.annotation.Produces;
import scmspain.karyon.restrouter.core.MethodParameterResolver;
import scmspain.karyon.restrouter.core.ResourceLoader;
import scmspain.karyon.restrouter.core.URIParameterParser;
import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.exception.UnsupportedFormatException;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withReturnType;

@Singleton
public class RestRouterScanner {

  public static final String BASE_PACKAGE_PROPERTY = "com.scmspain.karyon.rest.property.packages";
  private final Injector injector;
  private final URIParameterParser parameterParser;
  private final RestUriRouter<ByteBuf, ByteBuf> restUriRouter;
  private MethodParameterResolver rmParameterInjector;

  /**
   * Wrapper class, used like a struct to encapsulate info regarding
   * an endpoint method, and for the functional mapping and sorting below
   */
  private class EndpointDefinition {
    String uri;
    String verb;
    Method method;
    Class<?> klass;
    CustomSerialization customSerialization;

    EndpointDefinition(Method method, String uri, String verb, CustomSerialization customSerialization) {
      this.method = method;
      this.klass = method.getDeclaringClass();
      this.uri = uri;
      this.verb = verb;
      this.customSerialization = customSerialization;
    }
  }

  @Inject
  public RestRouterScanner(Injector inject,
                           URIParameterParser parameterParser,
                           MethodParameterResolver rmParameterInjector,
                           ResourceLoader resourceLoader,
                           RestUriRouter<ByteBuf, ByteBuf> restUriRouter) {

    this.restUriRouter = restUriRouter;
    this.injector = inject;
    this.parameterParser = parameterParser;
    this.rmParameterInjector = rmParameterInjector;

    String basePackage = ConfigurationManager.getConfigInstance().getString(BASE_PACKAGE_PROPERTY);
    Set<Class<?>> annotatedTypes = resourceLoader.find(basePackage, Endpoint.class);

    annotatedTypes.stream()
        .flatMap(klass ->
            getAllMethods(klass,
                Predicates.and(withModifier(Modifier.PUBLIC), withAnnotation(Path.class)),
                withReturnType(Observable.class)
            ).stream())
        .map(method -> {
          Path path = method.getAnnotation(Path.class);
          return new EndpointDefinition(method, path.value(), path.method(), path.customSerialization());
        })
        //Double sorting, so we get the precedence right
        .sorted((endpoint1, endpoint2) -> endpoint1.uri.indexOf("{") - endpoint2.uri.indexOf("{"))
        .sorted((endpoint1, endpoint2) -> endpoint1.uri.compareTo(endpoint2.uri))
        .forEach(this::configureEndpoint);
  }

  private void configureEndpoint(EndpointDefinition pathDefinition) {
    Method method = pathDefinition.method;

    Endpoint endpoint = pathDefinition.klass.getAnnotation(Endpoint.class);

    // If produces get the list media types, if not it returns an empty list
    List<String> producesTypes = Stream.of(method.getAnnotations())
        .filter(a -> a instanceof Produces)
        .map(Produces.class::cast)
        .map(Produces::value)
        .map(Arrays::asList)
        .findFirst()
        .orElse(Collections.emptyList());

    String uriRegex = parameterParser.getUriRegex(pathDefinition.uri);

    boolean customSerialization = getCustomSerialization(endpoint.customSerialization(), pathDefinition.customSerialization);

    restUriRouter.addUriRegex(uriRegex, pathDefinition.verb,
        producesTypes, customSerialization,
        (request, response) -> processRouteHandler(pathDefinition, method, request, response)
    );
  }

  private boolean getCustomSerialization(boolean endPointCustom, CustomSerialization methodCustomSerialization) {
    switch (methodCustomSerialization) {
      case TRUE:
        return true;

      case FALSE:
        return false;

      default:
        return endPointCustom;
    }
  }

  public RestUriRouter<ByteBuf, ByteBuf> getRestUriRouter() {
    return restUriRouter;
  }

  private Observable<Object> processRouteHandler(
      EndpointDefinition endpoint,
      Method method,
      HttpServerRequest<ByteBuf> request,
      HttpServerResponse<ByteBuf> response) {

    try {
      Map<String, String> params = parameterParser.getParams(endpoint.uri, request.getUri());
      Map<String, List<String>> queryParams = request.getQueryParameters();
      Object[] invokeParams = rmParameterInjector.resolveParameters(
          method,
          request,
          response,
          params,
          queryParams);

      return (Observable) method.invoke(injector.getInstance(endpoint.klass), invokeParams);

    } catch (IllegalAccessException e) {
      //Should never get here
      response.setStatus(HttpResponseStatus.FORBIDDEN);
      throw new RuntimeException("Exception invoking method: " + method.toString());
    } catch (ParamAnnotationException e) {
      response.setStatus(HttpResponseStatus.BAD_REQUEST);
      return Observable.empty();
    } catch (UnsupportedFormatException e) {
      response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      throw new RuntimeException(
          String.format("Impossible to resolve params in method \"%s\" ",
              method.toString()), e);
    } catch (InvocationTargetException e) {
      response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      throw new RuntimeException("Exception invoking method " + method.toString(), e);
    }
  }


}
