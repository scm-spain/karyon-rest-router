package scmspain.karyon.restrouter;


import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.core.MethodParameterResolver;
import scmspain.karyon.restrouter.core.ResourceLoader;
import scmspain.karyon.restrouter.core.URIParameterParser;
import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;

import static org.reflections.ReflectionUtils.*;

public class RestBasedRouter implements RequestHandler<ByteBuf, ByteBuf> {

  public static final String BASE_PACKAGE_PROPERTY = "com.scmspain.karyon.rest.property.packages";
  private final Injector injector;
  private final URIParameterParser parameterParser;
  private final RestUriRouter<ByteBuf, ByteBuf> delegate = new RestUriRouter<ByteBuf, ByteBuf>();
  private MethodParameterResolver rmParameterInjector;
  private ResourceLoader resourceLoader;

  /**
   * Wrapper class, used like a struct to encapsulate info regarding
   * an endpoint method, and for the functional mapping and sorting below
   */
  private class EndpointDefinition {
    String uri;
    String verb;
    Method method;
    Class<?> klass;

    EndpointDefinition(Method method, String uri, String verb) {
      this.method = method;
      this.klass = method.getDeclaringClass();
      this.uri = uri;
      this.verb = verb;
    };
  }

  @Inject
  public RestBasedRouter(Injector inject, URIParameterParser parameterParser,
                         MethodParameterResolver rmParameterInjector, ResourceLoader resourceLoader) {
    this.injector = inject;
    this.parameterParser = parameterParser;
    this.rmParameterInjector = rmParameterInjector;
    this.resourceLoader = resourceLoader;

    String basePackage = ConfigurationManager.getConfigInstance().getString(BASE_PACKAGE_PROPERTY);
    Set<Class<?>> annotatedTypes = resourceLoader.find(basePackage, Endpoint.class);

    annotatedTypes.stream()
        .flatMap(klass->
            getAllMethods(klass,
                Predicates.and(withModifier(Modifier.PUBLIC), withAnnotation(Path.class)),
                withReturnType(Observable.class)
            ).stream())
        .map(method -> {
          Path path = method.getAnnotation(Path.class);
          return new EndpointDefinition(method, path.value(), path.method());
        })
        //Double sorting, so we get the precedence right
        .sorted((endpoint1, endpoint2) -> endpoint1.uri.indexOf("{") - endpoint2.uri.indexOf("{"))
        .sorted((endpoint1, endpoint2) -> endpoint1.uri.compareTo(endpoint2.uri))
        .forEach(endpoint -> {
          Method method = endpoint.method;
          String uriRegex = parameterParser.getUriRegex(endpoint.uri);
          delegate.addUriRegex(uriRegex, endpoint.verb, (request, response) -> {
            try {
              Map<String, String> params = parameterParser.getParams(endpoint.uri, request.getUri());
              Map<String, List<String>> queryParams = request.getQueryParameters();
              Object[] invokeParams = rmParameterInjector.resolveParameters(method, request, response, params, queryParams);
              return (Observable) method.invoke(injector.getInstance(endpoint.klass), invokeParams);
            } catch (IllegalAccessException e) {
              //Should never get here
              response.setStatus(HttpResponseStatus.FORBIDDEN);
              throw new RuntimeException("Exception invoking method: " + method.toString());
            } catch (ParamAnnotationException e) {
              response.setStatus(HttpResponseStatus.BAD_REQUEST);
              return Observable.empty();
            } catch (InvocationTargetException e) {
              response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
              throw new RuntimeException("Exception invoking method " + method.toString(), e);
            }
          });

        });
  }



  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    return delegate.handle(request, response);
  }


}
