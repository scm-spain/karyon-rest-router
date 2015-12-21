package scmspain.karyon.restrouter.exception;

/**
 * Exception when the Accept header is invalid
 */
public class InvalidAcceptHeaderException extends KaryonRestRouterException {
  public InvalidAcceptHeaderException(Throwable cause) {
    super(cause);
  }
}
