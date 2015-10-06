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
import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.core.MethodParameterResolver;
import scmspain.karyon.restrouter.core.ResourceLoader;
import scmspain.karyon.restrouter.core.URIParameterParser;
import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.exception.HandlerNotFoundException;
import scmspain.karyon.restrouter.exception.InvalidAcceptHeaderException;
import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.exception.UnsupportedFormatException;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.Route;
import scmspain.karyon.restrouter.transport.http.RouteInterceptorSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withReturnType;

public class RestBasedRouter implements RequestHandler<ByteBuf, ByteBuf> {

  public static final String BASE_PACKAGE_PROPERTY = "com.scmspain.karyon.rest.property.packages";
  private final Injector injector;
  private final URIParameterParser parameterParser;
  private final RestUriRouter<ByteBuf, ByteBuf> delegate = new RestUriRouter<ByteBuf, ByteBuf>();
  private MethodParameterResolver rmParameterInjector;
  private ResourceLoader resourceLoader;
  private RouteInterceptorSupport routeInterceptorSupport;
  private ErrorHandler errorHandler;
  private Serializer serializer;
  private String defaultContentType;

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
  public RestBasedRouter(Injector inject,
                         URIParameterParser parameterParser,
                         MethodParameterResolver rmParameterInjector,
                         ResourceLoader resourceLoader,
                         RouteInterceptorSupport routeInterceptorSupport) {

    this.injector = inject;
    this.parameterParser = parameterParser;
    this.rmParameterInjector = rmParameterInjector;
    this.resourceLoader = resourceLoader;
    this.routeInterceptorSupport = routeInterceptorSupport;

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
            return processRouteHandler(endpoint, method, request, response);
          });

        });
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


  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    Observable<Object> resultObs;
    String contentType = defaultContentType;
    List<String> supportedContentTypes = serializer.getSupportedContentTypes();

    try {
      // si no hay handler deberia tirar exception HandlerNotFoundE
      Route<ByteBuf, ByteBuf> handler =  delegate.findBestMatch(request, response)
          .orElseThrow(HandlerNotFoundException::new);

      try {
        if (handler.isBasedOnSerializers()) {

          contentType = acceptNegociation(supportedContentTypes);
        }
        resultObs = handler.process(request, response);

      } catch (CannotSerializeException|InvalidAcceptHeaderException e) {
        resultObs = Observable.error(e);
      }

      // TODO: resultObs has errors
      resultObs = resultObs.onErrorReturn(throwable ->
        errorHandler.handleError(throwable, /*canSerializeError = */ handler.isBasedOnSerializers())
      );

      if (handler.isBasedOnSerializers()) {
        return serializer.serialize(resultObs, contentType);

      } else {
        // FIXME: Type generic type checking
        return (Observable)resultObs;
      }

    } catch (HandlerNotFoundException e) {
      // TODO: Revisar si enviar el status code aqui
      response.setStatus(HttpResponseStatus.NOT_FOUND);

      try{
        contentType = acceptNegociation(supportedContentTypes);
      } catch (CannotSerializeException|InvalidAcceptHeaderException ex) {
        contentType = defaultContentType;
      }

      // TODO: enviar DTO de error en vez de empty
      return serializer.serialize(Observable.empty(), contentType);

    }catch(Exception e) {
      // TODO: send error 500
      throw e;
    }
  }

  // TODO: Implement
  private String acceptNegociation(List<String> supportedContentTypes) {
    return null;
  }

}
