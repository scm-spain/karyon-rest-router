package scmspain.karyon.restrouter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.netflix.hystrix.contrib.rxnetty.metricsstream.HystrixMetricsStreamHandler;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.RequestHandler;

public class RouteHandlerProvider implements Provider<RequestHandler<ByteBuf, ByteBuf>> {

  private final Injector injector;

  @Inject
  public RouteHandlerProvider(Injector injector) {
    this.injector = injector;
  }

  @Override
  public RequestHandler<ByteBuf, ByteBuf> get() {

    RestRouterHandler restRouterHandler = this.injector.getInstance(RestRouterHandler.class);

    if (isMetricsStreamEnabled()) {
      return new HystrixMetricsStreamHandler<>(restRouterHandler);
    }

    return restRouterHandler;
  }

  public boolean isMetricsStreamEnabled() {
    return true;
  }
}
