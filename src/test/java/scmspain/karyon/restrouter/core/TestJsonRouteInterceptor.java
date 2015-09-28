package scmspain.karyon.restrouter.core;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.codehaus.jackson.map.ObjectMapper;
import rx.Observable;
import scmspain.karyon.restrouter.transport.http.RouteInterceptor;

import java.io.IOException;

/**
 * Created by borja.vazquez on 25/9/15.
 */
public class TestJsonRouteInterceptor implements RouteInterceptor<Void> {

  /**
   * Serialize the given Object to a JSON String
   * @param obj
   * @return String representing the given Object as Json
   */
  public String toJson(Object obj) {
    try {
      if (obj == null) {
        throw new RuntimeException("Object to serialize cannot be null");
      }

      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(obj);
    } catch (IOException e) {
      throw new RuntimeException("Error serializing the handler return value: " + obj, e);
    }
  }


  @Override
  public Observable<Void> intercept(Observable<Object> result, HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    return result
        .map(this::toJson)
        .flatMap(response::writeStringAndFlush);
  }
}
