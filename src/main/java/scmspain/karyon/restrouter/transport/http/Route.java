package scmspain.karyon.restrouter.transport.http;

import com.google.common.collect.ImmutableSet;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.interceptor.InterceptorKey;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class Route<I, O> {

  private final InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key;
  private final RouteHandler<I, O> routeHandler;
  private Set<String> produces = new HashSet<>();
  private boolean customSerialization;
  private String name;

  public Route(String name, InterceptorKey<HttpServerRequest<I>,
               HttpKeyEvaluationContext> key, Collection<String> produces,
               boolean customSerialization, RouteHandler<I, O> routeHandler) {

    this.name = name;
    this.key = key;
    this.routeHandler = routeHandler;
    this.customSerialization = customSerialization;
    this.produces = ImmutableSet.copyOf(produces);
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

  public boolean hasCustomSerialization() {
    return customSerialization;
  }

  public String getName() {
    return name;
  }

}
