package scmspain.karyon.restrouter.handlers;

import rx.Observable;

/**
 * Created by pablo.diaz on 6/10/15.
 */
public interface ErrorHandler {
  Observable<Object> handleError(Throwable throwable, StatusCodeSetter statusCode);

}
