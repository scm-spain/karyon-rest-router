package scmspain.karyon.restrouter.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import scmspain.karyon.restrouter.exception.RouteNotFoundException;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class RestUriRouter<I, O> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestUriRouter.class);

  private final CopyOnWriteArrayList<Route<I,O>> routes;

  public RestUriRouter() {
    routes = new CopyOnWriteArrayList<>();
  }

  public Observable<Object> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) throws RouteNotFoundException {

    Optional<Route<I,O>> bestRoute = findBestMatch(request, response);

    return bestRoute
        .map(r -> r.getHandler().process(request, response))
            .orElseThrow(RouteNotFoundException::new);
  }

  /**
   * Add a new URI regex -&lt; Handler route to this router.
   *
   * @param name the name of the route, just for logging purposes
   * @param uriRegEx URI regex to match
   * @param handler Request handler.
   * @param verb Request verb.
   * @param produces the list of content types the route may return.
   * @return The updated router.
   */
  public RestUriRouter<I, O> addUriRegex(String name, String uriRegEx, String verb, Collection<String> produces,
                                         boolean custom, RouteHandler<I, O> handler) {
    EnhancedRegexUriConstraintKey<I> interceptorKey =
        new EnhancedRegexUriConstraintKey<>(uriRegEx, verb);

    Route<I, O> route = new Route<>(name, interceptorKey, produces, custom, handler);

    routes.add(route);

    LOGGER.info(String.format("Route registered [%s|%s|%s]", verb, uriRegEx, route.getName()));

    return this;
  }

  /**
   * Find the best route for handling a request
   * @param request the request
   * @param response the response
   */
  public Optional<Route<I,O>> findBestMatch(HttpServerRequest<I> request, HttpServerResponse<O> response){
    HttpKeyEvaluationContext context = new HttpKeyEvaluationContext(response.getChannel());

    return routes.stream()
        .filter(route -> route.getKey().apply(request, context))
        .findFirst();
  }

  public List<Route<I, O>> getRoutes() {
    return routes;
  }
}
