package scmspain.karyon.restrouter.core;

import java.util.Map;
import scmspain.karyon.restrouter.annotation.PathParam;
import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.exception.PathParamNotFoundException;

public class PathParamAnnotation implements ParamAnnotation {


  private final PathParam annotation;
  private Map<String, String> pathParams;

  public PathParamAnnotation(PathParam annotation,Map<String, String> pathParams) {
    this.annotation = annotation;
    this.pathParams = pathParams;
  }

  @Override
  public String getParameterValue() throws ParamAnnotationException {
    String pathParamValue = pathParams.get(annotation.value());
    if (pathParamValue == null) throw new PathParamNotFoundException();
    return pathParamValue;
  }
}
