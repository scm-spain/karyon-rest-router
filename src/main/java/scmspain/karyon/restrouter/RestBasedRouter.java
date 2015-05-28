package scmspain.karyon.restrouter;


import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.http.SimpleUriRouter;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public class RestBasedRouter implements RequestHandler<ByteBuf, ByteBuf> {

    public static final String BASE_PACKAGE_PROPERTY        =   "com.scmspain.karyon.rest.property.packages";
    private final Injector injector;
    private final SimpleUriRouter<ByteBuf, ByteBuf> delegate = new SimpleUriRouter<ByteBuf, ByteBuf>();

    @Inject
    public RestBasedRouter(Injector inject){

        this.injector = inject;
        String basePackage = ConfigurationManager.getConfigInstance().getString(BASE_PACKAGE_PROPERTY);

        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> annotatedTypes =  reflections.getTypesAnnotatedWith(Endpoint.class);

        for(Class endpoint:annotatedTypes){
            Set<Method> endpointMethods = ReflectionUtils.getAllMethods(endpoint,
                    Predicates.and(
                            ReflectionUtils.withModifier(Modifier.PUBLIC),
                            ReflectionUtils.withAnnotation(Path.class),
                            ReflectionUtils.withParameters(HttpServerRequest.class, HttpServerResponse.class)),
                    ReflectionUtils.withReturnType(Observable.class));

            for(Method method:endpointMethods){
                Path path = method.getAnnotation(Path.class);
                String uri = path.value();

                delegate.addUri(uri, (request, response) -> {
                    try {
                        return (Observable) method.invoke(injector.getInstance(endpoint.newInstance().getClass()), request, response);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Exception invoking method " + method.toString(), e);
                    } catch (InstantiationException e) {
                        //No default constructor?
                        throw new RuntimeException("Could not instantiate endpoint, make sure you have a default constructor at " + endpoint.getName(), e);

                    } catch (IllegalAccessException e) {
                        //Should never get here
                        throw new RuntimeException("Exception invoking method: " + method.toString());
                    }
                });
            }
        }




    }

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        return delegate.handle(request, response);
    }


}
