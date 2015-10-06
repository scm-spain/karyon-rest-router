package scmspain.karyon.restrouter.transport.http;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.interceptor.InterceptorKey;
import rx.Observable;

import java.util.List;


public class Route<I, O> {

  private final InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key;
  private final RouteHandler<I, O> routeHandler;
  private List<String> produces;
  private boolean custom;

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

  public List<String> getProduces() {
    return produces;
  }

  public boolean isCustom() {
    return custom;
  }
}
