package scmspain.karyon.restrouter.exception;

/**
 * Created by pablo.diaz on 21/12/15.
 */
public class KaryonRestRouterException extends Exception{

  public KaryonRestRouterException() {
  }

  public KaryonRestRouterException(String message) {
    super(message);
  }

  public KaryonRestRouterException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public KaryonRestRouterException(Throwable cause) {
    super(cause);
  }
}
