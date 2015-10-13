package scmspain.karyon.restrouter.endpoint;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;

@Endpoint
public class MyEndpoint {

  @Path(value = "/test", method="GET")
  public Observable<String> test(HttpServerResponse<ByteBuf> response) {
    return Observable.just("hi");
  }
}
