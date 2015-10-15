package scmspain.karyon.restrouter.exception;

public class CannotSerializeException extends RuntimeException {
  public CannotSerializeException(String message) {
    super(message);
  }

  public CannotSerializeException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
