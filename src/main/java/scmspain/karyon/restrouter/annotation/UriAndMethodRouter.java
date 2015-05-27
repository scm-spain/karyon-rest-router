package scmspain.karyon.restrouter.annotation;

import netflix.karyon.transport.http.SimpleUriRouter;

/**
 * Created by ramonriusgrasset on 18/02/15.
 */
public class UriAndMethodRouter<I, O> extends SimpleUriRouter<I, O> {

//    /**
//     * Add a new URI -> Handler route to this router.
//     * @param uri URI to match.
//     * @param handler Request handler.
//     * @return The updated router.
//     */
//    public SimpleUriRouter<I, O> addUriAndMethod(String uri, HttpMethod method, RequestHandler<I, O> handler) {
//        super.routes.add(new Route(new UriAndMethodKey<I>(uri, method), handler));
//        return this;
//    }


}
