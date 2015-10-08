package scmspain.karyon.restrouter.handlers;

import rx.Observable;

/**
 * Created by borja.vazquez on 7/10/15.
 */
public class ErrorHandlerImpl implements ErrorHandler {
  @Override
  public Observable<Object> handleError(Throwable throwable, StatusCodeSetter statusCode) {
    return null;
  }
}
