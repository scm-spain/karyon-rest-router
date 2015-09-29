package scmspain.karyon.restrouter.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.interceptor.InterceptorKey;
import rx.Observable;


public class Route<I, O> {

  private final InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key;
  private final RouteHandler<I, O> routeHandler;

  public Route(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key,
               RouteHandler<I, O> routeHandler) {
    this.key = key;
    this.routeHandler = routeHandler;
  }

  public InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> getKey() {
    return key;
  }

  public RouteHandler<I, O> getHandler() {
    return routeHandler;
  }

}
