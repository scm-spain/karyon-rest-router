package scmspain.karyon.restrouter;

import com.google.inject.Singleton;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import scmspain.karyon.restrouter.annotation.PathParam;
import scmspain.karyon.restrouter.annotation.QueryParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class RestMethodParameterInjector {

    public Object[] resolveParameters(Method method, HttpServerRequest request, HttpServerResponse response,Map<String,String> params){

        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        Class[] parametersTypes = method.getParameterTypes();
        Object[] parameters = new Object[parametersTypes.length];

        for(int i=0;i<parametersTypes.length;i++){

            Class param = parametersTypes[i];
            if(param.isAssignableFrom(request.getClass())){
                parameters[i] = request;
            }else if(param.isAssignableFrom(response.getClass())){
                parameters[i] = response;
            }else{
                String value = null;
                Optional<PathParam> pathParam = findParameterAnnotations(parametersAnnotations[i],PathParam.class);
                if(pathParam.isPresent()){
                    String pathParamValue = params.get(pathParam.get().value());
                    //TODO ADD ALL EXCEPTIONS FOR CONVERSIONS
                    if(pathParamValue == null) throw new RuntimeException("Path param with incorrect value");

                    value = pathParamValue;

                }
                else{
                    Optional<QueryParam> queryParam = findParameterAnnotations(parametersAnnotations[i],QueryParam.class);
                    if(queryParam.isPresent()){
                        Map<String,List<String>> queryParams = request.getQueryParameters();
                        List<String> values = queryParams.get(queryParam.get().value());

                        String queryParamValue = null;
                        if( values != null && !values.isEmpty() ){
                            queryParamValue = values.get(0);
                        }
                        //TODO ADD ALL EXCEPTIONS FOR CONVERSIONS
                        if(queryParamValue == null) {
                            if( !QueryParam.DEFAULT_VALUE.equals(queryParam.get().defaultValue()) ){
                                queryParamValue = queryParam.get().defaultValue();
                            }
                            else if (queryParam.get().required()) {
                                //TODO ADD ALL EXCEPTIONS FOR CONVERSIONS
                                throw new RuntimeException();
                            }
                        }

                        value = queryParamValue;

                    }

                }


                if (param.isAssignableFrom(String.class)){
                    parameters[i] = value;
                }else if(param.isAssignableFrom(int.class) || param.isAssignableFrom(Integer.class)){
                    parameters[i] = Integer.valueOf(value);
                }
            }
        }

        return parameters;
    }


    public <T> Optional<T> findParameterAnnotations(Annotation[] annotations,Class<T> annotationClass){

        for (int i = 0; i < annotations.length; i++) {
            if(annotationClass.isInstance(annotations[i])){
                return Optional.of(annotationClass.cast(annotations[i]));
            }
        }
        return Optional.empty();
    }



}
