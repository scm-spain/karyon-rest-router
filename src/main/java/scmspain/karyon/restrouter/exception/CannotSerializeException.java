package scmspain.karyon.restrouter.exception;

/**
 * Exception when the Accept Header sent cannot be handle by any of the configured Serializers
 */
public class CannotSerializeException extends RuntimeException {
  public CannotSerializeException(String message) {
    super(message);
  }

  public CannotSerializeException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
