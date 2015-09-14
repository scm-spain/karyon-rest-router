package scmspain.karyon.restrouter.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import scmspain.karyon.restrouter.annotation.PathParam;
import scmspain.karyon.restrouter.annotation.QueryParam;
import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.exception.UnsupportedFormatException;

@Singleton
public class MethodParameterResolver {

  private ParamTypeResolver paramTypeResolver;

  @Inject
  public MethodParameterResolver(ParamTypeResolver paramTypeResolver){
    this.paramTypeResolver = paramTypeResolver;
  }

  public Object[] resolveParameters(
    Method method,
    HttpServerRequest request,
    HttpServerResponse response,
    Map<String, String> pathParams,
    Map<String, List<String>> queryParams
  ) throws ParamAnnotationException, UnsupportedFormatException
  {

    Annotation[][] parametersAnnotations = method.getParameterAnnotations();
    Class[] parametersTypes = method.getParameterTypes();
    Object[] parameters = new Object[parametersTypes.length];

    for (int i = 0; i < parametersTypes.length; i++) {

      Class param = parametersTypes[i];
      if (param.isAssignableFrom(request.getClass())) {
        parameters[i] = request;
      } else if (param.isAssignableFrom(response.getClass())) {
        parameters[i] = response;
      } else {
        String value = injectParameterValueAnnotation(request, pathParams, queryParams, parametersAnnotations[i]);
        parameters[i] = paramTypeResolver.resolveValueType(param,value);
      }
    }

    return parameters;
  }

  /*
      Return the value referenced in annotation
   */
  private String injectParameterValueAnnotation(HttpServerRequest request, Map<String, String> pathParams, Map<String, List<String>> queryParams, Annotation[] parametersAnnotation) throws ParamAnnotationException {

    if(parametersAnnotation.length>1) throw new ParamAnnotationException("Only one annotation by param are supported!");
    if(parametersAnnotation.length<1) throw new ParamAnnotationException("Parameter without annotation");

    ParamAnnotation paramAnnotation;
    Annotation firstAnnotation = parametersAnnotation[0];

    if(PathParam.class.isInstance(firstAnnotation)){
      paramAnnotation = new PathParamAnnotation((PathParam) firstAnnotation, pathParams);

    }else if(QueryParam.class.isInstance(firstAnnotation)){
      paramAnnotation = new QueryParamAnnotation((QueryParam) firstAnnotation,queryParams);
    }else{
      return null;
    }

    return paramAnnotation.getParameterValue();
  }

}
