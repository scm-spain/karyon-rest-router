package scmspain.karyon.restrouter.exception;

/**
 * Exception when the Accept header is invalid
 */
public class InvalidAcceptHeaderException extends RuntimeException {
  public InvalidAcceptHeaderException(Throwable cause) {
    super(cause);
  }
}
