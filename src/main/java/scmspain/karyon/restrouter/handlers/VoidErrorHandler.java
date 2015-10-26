package scmspain.karyon.restrouter.handlers;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

/**
 * Error handler that doesn't handle anything it is just an empty implementation, and it's the
 * default implementation
 * @param <T> the generic type of the {@link HttpServerRequest}
 */
public class VoidErrorHandler<T> implements ErrorHandler<T> {
  private static final VoidErrorHandler<Object> INSTANCE = new VoidErrorHandler<>();

  @Override
  public Observable<Object> handleError(HttpServerRequest<T> request, Throwable throwable, StatusCodeSetter statusCode) {
    return Observable.error(throwable);
  }

  @SuppressWarnings("unchecked")
  public static <T> VoidErrorHandler<T> getInstance() {
    return (VoidErrorHandler<T>) INSTANCE;
  }
}
