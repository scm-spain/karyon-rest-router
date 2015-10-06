package scmspain.karyon.restrouter;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;
import scmspain.karyon.restrouter.exception.RouteNotFoundException;

/**
 * Created by pablo.diaz on 6/10/15.
 */

public class RouteNotFoundHandler<I, O> implements scmspain.karyon.restrouter.transport.http.RouteHandler<I,O> {
  @Override
  public Observable<Object> process(HttpServerRequest<I> request, HttpServerResponse<O> response) {
    return Observable.error(new RouteNotFoundException());
  }
}
