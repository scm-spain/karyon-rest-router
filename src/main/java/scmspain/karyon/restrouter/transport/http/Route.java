package scmspain.karyon.restrouter.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.interceptor.InterceptorKey;

import java.util.HashSet;
import java.util.Set;


public class Route<I, O> {

  private final InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key;
  private final RouteHandler<I, O> routeHandler;
  private Set<String> produces = new HashSet<>();
  private boolean custom;

  public Route(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key,
               RouteHandler<I, O> routeHandler) {
    this.key = key;
    this.routeHandler = routeHandler;
    this.custom = true;
  }

  public InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> getKey() {
    return key;
  }

  public RouteHandler<I, O> getHandler() {
    return routeHandler;
  }

  public Set<String> getProduces() {
    return produces;
  }

  public boolean isCustom() {
    return custom;
  }
}
