package scmspain.karyon.restrouter.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

/**
 * Created by borja.vazquez on 25/9/15.
 */
public interface RouteHandler<I, O> {

  Observable<Object> process(HttpServerRequest<I> request, HttpServerResponse<O> response);
}
