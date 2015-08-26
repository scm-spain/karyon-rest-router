package scmspain.karyon.restrouter.core;

import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.exception.PathParamNotFoundException;
import scmspain.karyon.restrouter.exception.QueryParamRequiredNotFoundException;

interface ParamAnnotation {
  public String getParameterValue() throws PathParamNotFoundException, QueryParamRequiredNotFoundException, ParamAnnotationException;
}
