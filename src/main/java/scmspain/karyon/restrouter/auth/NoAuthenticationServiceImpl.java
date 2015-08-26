package scmspain.karyon.restrouter.auth;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

/**
 * Created by ramonriusgrasset on 18/02/15.
 */
public class NoAuthenticationServiceImpl implements AuthenticationService {

  @Override
  public Observable<Boolean> authenticate(HttpServerRequest<ByteBuf> request) {
    return Observable.just(Boolean.TRUE);
  }
}
