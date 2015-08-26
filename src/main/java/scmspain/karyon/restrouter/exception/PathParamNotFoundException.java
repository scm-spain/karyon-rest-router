package scmspain.karyon.restrouter.exception;

public class PathParamNotFoundException extends ParamAnnotationException {
  public PathParamNotFoundException() {
    super("URI Parameter not found!");
  }
}
