package scmspain.karyon.restrouter.handlers;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

/**
 * Interface of the ErrorHandler to handle errors generated in the routes
 * @param <T> Generic request type
 */
public interface ErrorHandler<T> {
  /**
   * Method to implement the error handle logic
   * @param request the request that generate this error
   * @param throwable the throwable type that generated the error
   * @param statusCode class to set the status code
   * @return An Observable of a generic type that will be serialized
   */
  Observable<?> handleError(HttpServerRequest<T> request, Throwable throwable, StatusCodeSetter statusCode);

}
