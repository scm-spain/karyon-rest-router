package scmspain.karyon.restrouter.exception;


public class UnsupportedFormatException extends KaryonRestRouterException {
  public UnsupportedFormatException(String classTryingToFormat) {
    super(String.format("Class \"%s\" is not supported.", classTryingToFormat ));
  }
}
