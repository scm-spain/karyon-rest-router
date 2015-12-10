package scmspain.karyon.restrouter;


import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.annotation.Produces;
import scmspain.karyon.restrouter.core.MethodParameterResolver;
import scmspain.karyon.restrouter.core.ResourceLoader;
import scmspain.karyon.restrouter.core.URIParameterParser;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.Route;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withReturnType;

@Singleton
public class RestRouterScanner {
  private static Logger logger = LoggerFactory.getLogger(RestRouterScanner.class);


  public static final String BASE_PACKAGE_PROPERTY = "com.scmspain.karyon.rest.property.packages";
  private final Injector injector;
  private final URIParameterParser parameterParser;
  private final RestUriRouter<ByteBuf, ByteBuf> restUriRouter;
  private MethodParameterResolver rmParameterInjector;

  /**
   * Wrapper class, used like a struct to encapsulate info regarding
   * an endpoint method, and for the functional mapping and sorting below
   */
  private class PathDefinition {
    String uri;
    String verb;
    Method method;
    Class<?> klass;

    PathDefinition(Method method, String uri, String verb) {
      this.method = method;
      this.klass = method.getDeclaringClass();
      this.uri = uri;
      this.verb = verb;
    }
  }

  @Inject
  private void validate(SerializeManager serializeManager) {
    List<Route<ByteBuf, ByteBuf>> routes = restUriRouter.getRoutes();

    for(Route<ByteBuf, ByteBuf> route: routes) {
      Set<String> produces = route.getProduces();
      boolean custom = route.isCustomSerialization();

      if(serializeManager.getSupportedMediaTypes().isEmpty() && !custom) {
        String message = "There isn't serializers configured with a serialized route '" + route.getName() + "'";

        RuntimeException e = new RuntimeException(message);
        logger.error("error", e);
        throw e;
      }

      if(!serializeManager.getSupportedMediaTypes().containsAll(produces)) {
        RuntimeException e = new RuntimeException("Exists a route with a produces that cannot be handle");
        logger.error("error", e);
        throw e;
      }

      if(!produces.isEmpty() && custom) {
        RuntimeException e = new RuntimeException("There is a route which is not serializable annotated with produce");
        logger.error("error", e);
        throw e;
      }

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

    Set<Class<?>> annotatedTypes = getEndpointClasses(resourceLoader);

    annotatedTypes.stream()
        .flatMap(klass ->
            getAllMethods(klass,
                Predicates.and(withModifier(Modifier.PUBLIC), withAnnotation(Path.class)),
                withReturnType(Observable.class)
            ).stream())
        .map(method -> {
          Path path = method.getAnnotation(Path.class);
          return new PathDefinition(method, path.value(), path.method());
        })
        //Double sorting, so we get the precedence right
        .sorted((endpoint1, endpoint2) -> endpoint1.uri.indexOf("{") - endpoint2.uri.indexOf("{"))
        .sorted((endpoint1, endpoint2) -> endpoint1.uri.compareTo(endpoint2.uri))
        .forEach(this::configurePath);
  }

  private Set<Class<?>> getEndpointClasses(ResourceLoader resourceLoader) {
    List<String> packagesList = Arrays.asList(ConfigurationManager.getConfigInstance().getStringArray(BASE_PACKAGE_PROPERTY));

    if(packagesList.isEmpty()) {
      logger.info("packages to scan: empty");
      packagesList = Collections.singletonList("");
    }

    return packagesList.stream()
          .map(String::trim)
          .map(packageName -> {
            logger.info("Scanning package: {}", packageName);
            return resourceLoader.find(packageName, Endpoint.class);
          })
          .reduce(new HashSet<>(), Sets::union);
  }

  private void configurePath(PathDefinition pathDefinition) {
    Method method = pathDefinition.method;

    boolean isCustomSerialization = isCustom(method);

    // If produces get the list media types, if not it returns an empty list
    List<String> producesTypes = Stream.of(method.getAnnotations())
        .filter(a -> a instanceof Produces)
        .map(Produces.class::cast)
        .map(Produces::value)
        .map(Arrays::asList)
        .findFirst()
        .orElse(Collections.emptyList());

    String uriRegex = parameterParser.getUriRegex(pathDefinition.uri);

    String name = pathDefinition.klass.getName() + "." + method.getName();

    restUriRouter.addUriRegex(name, uriRegex, pathDefinition.verb,
        producesTypes, isCustomSerialization,
        (request, response) -> processRouteHandler(pathDefinition, method, request, response)
    );
  }

  private boolean isCustom(Method method) {
    Class<?> returnType = method.getReturnType();

    if(Observable.class.isAssignableFrom(returnType)){
      Type genericReturnType = method.getGenericReturnType();
      ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;

      Type[] types = parameterizedType.getActualTypeArguments();
      return types.length == 1 && Void.class.getName().equals(types[0].getTypeName());

    } else {
      throw new RuntimeException("Unexpected return type in path " + returnType.getName());
    }
  }

  public RestUriRouter<ByteBuf, ByteBuf> getRestUriRouter() {
    return restUriRouter;
  }

  private Observable<Object> processRouteHandler(
      PathDefinition endpoint,
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

    } catch (Throwable e) {
      return Observable.error(e);
    }
  }


}
