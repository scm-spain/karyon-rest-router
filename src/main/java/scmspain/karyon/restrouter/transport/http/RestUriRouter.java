package scmspain.karyon.restrouter.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.interceptor.InterceptorKey;
import rx.Observable;
import scmspain.karyon.restrouter.exception.RouteNotFoundException;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public class RestUriRouter<I, O> {

  private final CopyOnWriteArrayList<Route<I,O>> routes;

  public RestUriRouter() {
    routes = new CopyOnWriteArrayList<>();
  }

  public Observable<Object> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
    Optional<Route<I,O>> bestRoute = findBestMatch(request, response);

    return bestRoute
        .map(r -> r.getHandler().process(request, response))
            .orElseThrow(RouteNotFoundException::new);
  }

  /**
   * Add a new URI regex -&lt; Handler route to this router.
   *
   * @param uriRegEx URI regex to match
   * @param handler Request handler.
   * @param verb Request verb.
   * @return The updated router.
   */
  public RestUriRouter<I, O> addUriRegex(String uriRegEx, String verb, Collection<String> produces,
                                         boolean custom, RouteHandler<I, O> handler) {
    EnhancedRegexUriConstraintKey<I> interceptorKey =
        new EnhancedRegexUriConstraintKey<>(uriRegEx, verb);

    routes.add(new Route<I, O>(interceptorKey, produces, custom, handler));

    return this;
  }

  /**
   * Find the best route for handling a request
   * @param request
   * @param response
   */
  public Optional<Route<I,O>> findBestMatch(HttpServerRequest<I> request, HttpServerResponse<O> response){
    HttpKeyEvaluationContext context = new HttpKeyEvaluationContext(response.getChannel());

    return routes.stream()
        .filter(route -> route.getKey().apply(request, context))
        .findFirst();
  }

}
