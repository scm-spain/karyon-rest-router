package scmspain.karyon.restrouter.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.interceptor.InterceptorKey;

/**
 * Created by victor.caldentey on 1/6/15.
 */
public class Route<I, O> {

  private final InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key;
  private final RequestHandler<I, O> handler;

  public Route(InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> key,
               RequestHandler<I, O> handler) {
    this.key = key;
    this.handler = handler;
  }

  public InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext> getKey() {
    return key;
  }

  public RequestHandler<I, O> getHandler() {
    return handler;
  }
}
