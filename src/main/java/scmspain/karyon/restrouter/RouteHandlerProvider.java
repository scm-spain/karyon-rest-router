package scmspain.karyon.restrouter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.contrib.rxnetty.metricsstream.HystrixMetricsStreamHandler;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.RequestHandler;

public class RouteHandlerProvider implements Provider<RequestHandler<ByteBuf, ByteBuf>> {

  public static final String PROP_EXPOSE_HYSTRIX_METRICS =
    "com.scmspain.karyon.rest.property.exposeHystrixMetrics";

  private final DynamicPropertyFactory properties = DynamicPropertyFactory.getInstance();
  private final RequestHandler<ByteBuf, ByteBuf> restRouterHandler;
  private final boolean defaultIsMetricsStreamEnabled = true;

  @Inject
  public RouteHandlerProvider(Injector injector) {
    restRouterHandler = injector.getInstance(RestRouterHandler.class);
  }

  @Override
  public RequestHandler<ByteBuf, ByteBuf> get() {
    if (isMetricsStreamEnabled()) {
      return new HystrixMetricsStreamHandler<>(restRouterHandler);
    }

    return restRouterHandler;
  }

  public boolean isMetricsStreamEnabled() {
    return properties.getBooleanProperty(
      PROP_EXPOSE_HYSTRIX_METRICS, defaultIsMetricsStreamEnabled).get();
  }
}
