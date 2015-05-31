package scmspain.karyon.restrouter;

import com.google.inject.Singleton;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Singleton
public class RestMethodParameterInjector {

    public Object[] resolveParameters(Method method, HttpServerRequest request, HttpServerResponse response,Map<String,String> params){


        Class[] parametersTypes = method.getParameterTypes();
        Object[] parameters = new Object[parametersTypes.length];

        for(int i=0;i<parametersTypes.length;i++){

            Class param = parametersTypes[i];
            if(param.isAssignableFrom(request.getClass())){
                parameters[i] = request;
            }else if(param.isAssignableFrom(response.getClass())){
                parameters[i] = response;
            }else if(param.isAssignableFrom(params.getClass())){
                parameters[i] = params;
            }
        }

        return parameters;
    }

}
