package scmspain.karyon.restrouter.exception;

/**
 * Created by pablo.diaz on 29/9/15.
 */
public class CannotSerializeException extends RuntimeException {
  public CannotSerializeException(String message) {
    super(message);
  }

  public CannotSerializeException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
