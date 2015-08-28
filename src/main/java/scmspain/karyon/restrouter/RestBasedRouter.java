package scmspain.karyon.restrouter;


import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
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
import org.reflections.ReflectionUtils;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.core.MethodParameterResolver;
import scmspain.karyon.restrouter.core.ResourceLoader;
import scmspain.karyon.restrouter.core.URIParameterParser;
import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;

public class RestBasedRouter implements RequestHandler<ByteBuf, ByteBuf> {

  public static final String BASE_PACKAGE_PROPERTY = "com.scmspain.karyon.rest.property.packages";
  private final Injector injector;
  private final URIParameterParser parameterParser;
  private final RestUriRouter<ByteBuf, ByteBuf> delegate = new RestUriRouter<ByteBuf, ByteBuf>();
  private MethodParameterResolver rmParameterInjector;
  private ResourceLoader resourceLoader;

  @Inject
  public RestBasedRouter(Injector inject, URIParameterParser parameterParser, MethodParameterResolver rmParameterInjector, ResourceLoader resourceLoader) {

    this.injector = inject;
    this.parameterParser = parameterParser;
    this.rmParameterInjector = rmParameterInjector;
    this.resourceLoader = resourceLoader;


    String basePackage = ConfigurationManager.getConfigInstance().getString(BASE_PACKAGE_PROPERTY);
    Set<Class<?>> annotatedTypes = resourceLoader.find(basePackage, Endpoint.class);

    for (Class endpoint : annotatedTypes) {
      Set<Method> endpointMethods = ReflectionUtils.getAllMethods(endpoint,
          Predicates.and(
              ReflectionUtils.withModifier(Modifier.PUBLIC),
              ReflectionUtils.withAnnotation(Path.class)),
          ReflectionUtils.withReturnType(Observable.class));

      for (Method method : endpointMethods) {
        Path path = method.getAnnotation(Path.class);
        String uri = path.value();
        String verb = path.method();
        String uriRegex = parameterParser.getUriRegex(uri);
        delegate.addUriRegex(uriRegex, verb, (request, response) -> {
          try {
            Map<String, String> params = parameterParser.getParams(uri, request.getUri());
            Map<String, List<String>> queryParams = request.getQueryParameters();
            Object[] invokeParams = rmParameterInjector.resolveParameters(method, request, response, params, queryParams);
            return (Observable) method.invoke(injector.getInstance(endpoint), invokeParams);
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
      }
    }


  }

  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    return delegate.handle(request, response);
  }


}
