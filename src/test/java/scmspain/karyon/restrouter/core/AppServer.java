package scmspain.karyon.restrouter.core;

import com.google.inject.Singleton;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.governator.annotations.Modules;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.archaius.ArchaiusBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scmspain.karyon.restrouter.KaryonRestRouterModule;
import scmspain.karyon.restrouter.endpoint.ExampleEndpointController;

@ArchaiusBootstrap
@KaryonBootstrap(name = "AppServer")
@Singleton
@Modules(include = {
    AppServer.KaryonRestRouterModuleImpl.class
})
public interface AppServer {
  class KaryonRestRouterModuleImpl extends KaryonRestRouterModule {
    public static final int DEFAULT_PORT = 8000;
    public static final int DEFAULT_THREADS_POOL_SIZE = 20;

    private static final Logger LOGGER = LoggerFactory.getLogger(KaryonRestRouterModuleImpl.class);
    private DynamicPropertyFactory properties = DynamicPropertyFactory.getInstance();

    @Override
    protected void configureServer() {

      int port = properties.getIntProperty("server.port", DEFAULT_PORT).get();
      int threads = properties.getIntProperty("server.threads", DEFAULT_THREADS_POOL_SIZE).get();

      server()
          .port(port)
          .threadPoolSize(threads);
    }

    @Override
    public void configure() {
      bind(ExampleEndpointController.class);
      super.configure();
    }
  }
}
