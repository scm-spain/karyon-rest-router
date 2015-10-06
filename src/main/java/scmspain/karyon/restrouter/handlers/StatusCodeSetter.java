package scmspain.karyon.restrouter.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;

@FunctionalInterface
public interface StatusCodeSetter {
  void set(HttpResponseStatus statusCode);
}
