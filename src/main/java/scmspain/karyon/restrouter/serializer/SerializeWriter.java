package scmspain.karyon.restrouter.serializer;

import com.google.common.net.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

/**
 * Created by pablo.diaz on 6/10/15.
 */
public class SerializeWriter {
  private HttpServerResponse<ByteBuf> response;

  public SerializeWriter(HttpServerResponse<ByteBuf> response, String contentType) {
    this.response = response;

    response.getHeaders().setHeader(HttpHeaders.CONTENT_TYPE, contentType);
  }

  public Observable<Void> write(byte[] bytes) {
    return response.writeBytesAndFlush(bytes);
  }
}
