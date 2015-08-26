package scmspain.karyon.restrouter.exception;

public class QueryParamRequiredNotFoundException extends ParamAnnotationException {

  public QueryParamRequiredNotFoundException(String parameterName) {
    super(String.format("QueryParameter %s required but not found!", parameterName));
  }
}
