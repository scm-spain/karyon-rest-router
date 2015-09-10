package scmspain.karyon.restrouter.exception;


public class UnsupportedFormatException extends ParamAnnotationException {
  public UnsupportedFormatException(String classTryingToFormat) {
    super(String.format("Class \"%s\" is not supported.", classTryingToFormat ));
  }
}
