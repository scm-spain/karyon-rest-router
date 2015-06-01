package scmspain.karyon.restrouter.transport.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import rx.Observable;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by victor.caldentey on 31/5/15.
 */
public class RestUriRouter<I, O> implements RequestHandler<I, O> {


    private final CopyOnWriteArrayList<Route> routes;

    public RestUriRouter() {
        routes = new CopyOnWriteArrayList<Route>();
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
        HttpKeyEvaluationContext context = new HttpKeyEvaluationContext(response.getChannel());
        for (Route route : routes) {
            if (route.getKey().apply(request, context)) {
                return route.getHandler().handle(request, response);
            }
        }

        // None of the routes matched.
        response.setStatus(HttpResponseStatus.NOT_FOUND);
        return response.close();
    }

    /**
     * Add a new URI regex -&lt; Handler route to this router.
     * @param uriRegEx URI regex to match
     * @param handler Request handler.
     * @param verb Request verb.
     * @return The updated router.
     */
    public RestUriRouter<I, O> addUriRegex(String uriRegEx , String verb, RequestHandler<I, O> handler) {
        routes.add(new Route(new EnhancedRegexUriConstraintKey<I>(uriRegEx, verb), handler));
        return this;
    }

}
