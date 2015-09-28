package scmspain.karyon.restrouter.transport.http;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by borja.vazquez on 25/9/15.
 */
public class RouteInterceptorSupport {

  private List<RouteInterceptor> routeInterceptorList = new ArrayList<>();

  /*public RouteInterceptorSupport() {
    routeInterceptorList.add((result, request, response) -> result.map(value -> null));
  }*/

  public void addInterceptor(RouteInterceptor routeInterceptor) {
    routeInterceptorList.add(routeInterceptor);
  }

  public Observable<Void> execute(Observable<Object> responseBodyObs, HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {

    if (routeInterceptorList.isEmpty()) {
      return responseBodyObs.flatMap(result -> {
        if (result instanceof Void) {
          //TODO: Check what to do here??
          return null;
        } else {
          return Observable.error(new RuntimeException("There are not RouteInterceptors defined."));
        }
      });
    }

   Observable<Object> interceptorResultObs = routeInterceptorList
        .stream()
        .reduce(
            responseBodyObs,
            (currentResponseBodyObs, interceptor) -> {
              return interceptor.intercept(currentResponseBodyObs, request, response);
            },
            (responseBodyObs1, responseBodyObs2) -> responseBodyObs2);


    return interceptorResultObs.map(result -> null);
  }
}
