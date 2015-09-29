package scmspain.karyon.restrouter.core;

import com.google.inject.Singleton;
import com.netflix.governator.annotations.Modules;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.archaius.ArchaiusBootstrap;
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

    @Override
    protected void configureServer() {

      server()
          .port(DEFAULT_PORT)
          .threadPoolSize(DEFAULT_THREADS_POOL_SIZE);

      this.addRouteInterceptor(new TestJsonRouteOutInterceptor());
    }

    @Override
    public void configure() {
      bind(ExampleEndpointController.class);
      super.configure();
    }
  }
}
