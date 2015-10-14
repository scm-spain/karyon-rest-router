package scmspain.karyon.restrouter.handlers;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;


public interface ErrorHandler<T, U> {
  Observable<U> handleError(HttpServerRequest<T> request, Throwable throwable, StatusCodeSetter statusCode);

}
