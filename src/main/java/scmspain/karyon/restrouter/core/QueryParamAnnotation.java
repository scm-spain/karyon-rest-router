package scmspain.karyon.restrouter.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import scmspain.karyon.restrouter.annotation.QueryParam;
import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.exception.QueryParamRequiredNotFoundException;

public class QueryParamAnnotation implements ParamAnnotation {
  private final QueryParam queryParam;
  private final Map<String, List<String>> queryParams;

  public QueryParamAnnotation(QueryParam queryParam, Map<String, List<String>> queryParams) {

    this.queryParam = queryParam;
    this.queryParams = queryParams;
  }

  @Override
  public String getParameterValue() throws ParamAnnotationException {
    Optional<List<String>> values = Optional.ofNullable(queryParams.get(queryParam.value()));

    if(queryParam.required() && !values.isPresent()) throw new QueryParamRequiredNotFoundException(queryParam.value());

    if(values.isPresent()){
      return values.get().stream().findFirst().get();
    }


    return queryParam.defaultValue();

  }
}
