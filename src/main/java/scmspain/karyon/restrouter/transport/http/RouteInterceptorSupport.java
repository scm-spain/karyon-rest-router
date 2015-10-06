package scmspain.karyon.restrouter.transport.http;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by borja.vazquez on 25/9/15.
 */
public class RouteInterceptorSupport {
  private static final Logger logger = LoggerFactory.getLogger(RouteInterceptorSupport.class);

  private List<RouteFilterChain> routeFilterChainList = new ArrayList<>();

  public RouteInterceptorSupport() {
    routeFilterChainList.add((result, request, response) -> result.flatMap(value -> {
      if (value == null) {
        return Observable.empty();
      } else {
        String message = "Cannot handle value of type as Observable type in endpoint return: " + value.getClass();
        logger.error(message);
        return Observable.error(new RuntimeException(message));
      }
    }));
  }

  public void addOutInterceptor(RouteFilterChain routeFilterChain) {
    routeFilterChainList.add(routeFilterChainList.size()-1, routeFilterChain);
  }

  /**
   * It executes all interceptor in order to the response body.
   * Return generic type will be Void for sure, because we add an interceptor at the end that will
   * convert the value to a Void.
   * @param responseBodyObs the responseBody Observable
   * @param request the request
   * @param response the response
   * @return the Void observable that karyon expects
   */
  @SuppressWarnings("unchecked")
  public Observable<Void> execute(Observable<?> responseBodyObs, HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    return (Observable<Void>)applyInterceptors(responseBodyObs, request, response, 0);

  }

  private Observable<?> applyInterceptors(Observable<?> responseBodyObs, HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response, int i) {
    if(i >= routeFilterChainList.size()) {
      return responseBodyObs;
    }

    RouteFilterChain routeFilterChain = routeFilterChainList.get(i);

    Observable<?> resultObs;
    try {
      resultObs = routeFilterChain.intercept(responseBodyObs, request, response);

    } catch(Throwable t) {
      /*
      Problem in interceptor, we encapsulate as another observable and send it to the next
      interceptor. Maybe some interceptor tries to do things withs errors.
       */
      resultObs = Observable.error(t);
    }

    return applyInterceptors(resultObs, request, response, i+1);
  }
}
