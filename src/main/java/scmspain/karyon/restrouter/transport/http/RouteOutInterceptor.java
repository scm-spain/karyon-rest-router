package scmspain.karyon.restrouter.transport.http;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

import java.util.Objects;

/**
 * Created by borja.vazquez on 25/9/15.
 */
public interface RouteOutInterceptor<T> {

  Observable<T> intercept(Observable<Object> responseBodyObs, HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response);
}
