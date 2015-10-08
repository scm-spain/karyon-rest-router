package scmspain.karyon.restrouter.serializer;

import com.google.common.net.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

import java.io.IOException;

public class SerializeWriter {
  private HttpServerResponse<ByteBuf> response;

  public SerializeWriter(HttpServerResponse<ByteBuf> response, String contentType) {
    this.response = response;
    if (contentType != null) {
      response.getHeaders().setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }
  }

  // TODO: Should be implemented using an static method?
  public Observable<Void> write(Object obj, Serializer serializer) {
    ByteBuf byteBuf = response.getAllocator().ioBuffer();

    try (ByteBufOutputStream outputStream = new ByteBufOutputStream(byteBuf)) {
      serializer.serialize(obj, outputStream);

    } catch (IOException e) {
      return Observable.error(e);
    }

    return response.writeBytesAndFlush(byteBuf);
  }
}
