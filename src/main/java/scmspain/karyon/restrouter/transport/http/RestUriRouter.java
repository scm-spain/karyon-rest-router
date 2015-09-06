package scmspain.karyon.restrouter.transport.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import rx.Observable;


public class RestUriRouter<I, O> implements RequestHandler<I, O> {

  private final CopyOnWriteArrayList<Route<I,O>> routes;

  public RestUriRouter() {
    routes = new CopyOnWriteArrayList<>();
  }

  @Override
  public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
    Optional<Route<I,O>> bestRoute = findBestMatch(request, response);

    return bestRoute
        .map(r -> r.getHandler().handle(request, response))
        .orElseGet(() -> {
          response.setStatus(HttpResponseStatus.NOT_FOUND);
          return response.close();
        });
  }

  /**
   * Add a new URI regex -&lt; Handler route to this router.
   *
   * @param uriRegEx URI regex to match
   * @param handler Request handler.
   * @param verb Request verb.
   * @return The updated router.
   */
  public RestUriRouter<I, O> addUriRegex(String uriRegEx, String verb, RequestHandler<I, O> handler) {
    routes.add(new Route(new EnhancedRegexUriConstraintKey<I>(uriRegEx, verb), handler));
    return this;
  }

  /**
   * Find the best route for handling a request
   * @param request
   * @param response
   */
  private Optional<Route<I,O>> findBestMatch(HttpServerRequest<I> request, HttpServerResponse<O> response){
    HttpKeyEvaluationContext context = new HttpKeyEvaluationContext(response.getChannel());

    return routes.stream()
        .filter(route -> route.getKey().apply(request, context))
        .findFirst();
  }

}
