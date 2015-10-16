package scmspain.karyon.restrouter.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * This interface is to define how a http status code can be setted.
 */
@FunctionalInterface
public interface StatusCodeSetter {
  /**
   * Sets the status code
   * @param statusCode the status code to set
   */
  void set(HttpResponseStatus statusCode);
}
