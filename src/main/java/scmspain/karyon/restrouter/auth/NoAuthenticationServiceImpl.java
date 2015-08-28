package scmspain.karyon.restrouter.auth;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;


public class NoAuthenticationServiceImpl implements AuthenticationService {

  @Override
  public Observable<Boolean> authenticate(HttpServerRequest<ByteBuf> request) {
    return Observable.just(Boolean.TRUE);
  }
}
