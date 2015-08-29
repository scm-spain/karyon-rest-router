package scmspain.karyon.restrouter.core;

import java.util.List;
import java.util.Map;
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
    List<String> values = queryParams.get(queryParam.value());

    String queryParamValue = null;
    if (values != null && !values.isEmpty()) {
      queryParamValue = values.get(0);
    }
    if (queryParamValue == null) {
      if (!QueryParam.DEFAULT_VALUE.equals(queryParam.defaultValue())) {
        queryParamValue = queryParam.defaultValue();
      } else if (queryParam.required()) {
        throw new QueryParamRequiredNotFoundException(queryParam.value());
      }
    }

    return queryParamValue;
  }
}
