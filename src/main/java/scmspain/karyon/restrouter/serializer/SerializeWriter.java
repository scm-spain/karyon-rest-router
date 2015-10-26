package scmspain.karyon.restrouter.serializer;

import com.google.common.net.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

import java.io.IOException;

/**
 * Class responsible of the write using a serializer
 */
public class SerializeWriter {
  private HttpServerResponse<ByteBuf> response;

  /**
   * Creates a write for a given {@link HttpServerResponse}
   * @param response the response
   * @param contentType the content type to serialize
   */
  public SerializeWriter(HttpServerResponse<ByteBuf> response, String contentType) {
    this.response = response;
    if (contentType != null) {
      response.getHeaders().setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }
  }

  /**
   * Writes the object to the response using a serializer
   * @param obj the object to serialize
   * @param serializer the serializer to use
   * @return an Observable with only onComplete if everything goes ok, or an onError is something
   * bad happens
   */
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
