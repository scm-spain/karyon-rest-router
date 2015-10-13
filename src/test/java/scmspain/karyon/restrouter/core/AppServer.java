package scmspain.karyon.restrouter.core;

import com.google.inject.Singleton;
import com.netflix.governator.annotations.Modules;
import io.netty.handler.codec.http.HttpResponseStatus;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.archaius.ArchaiusBootstrap;
import rx.Observable;
import scmspain.karyon.restrouter.KaryonRestRouterModule;
import scmspain.karyon.restrouter.endpoint.ExampleEndpointController;
import scmspain.karyon.restrouter.exception.RouteNotFoundException;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.handlers.StatusCodeSetter;
import scmspain.karyon.restrouter.serializer.Configuration;

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


      this.setConfiguration(Configuration.builder()
              .defaultContentType("application/json")
              .addSerializer(new JsonSerializer())
              .build()
      );

    }

    @Override
    public void configure() {
      bind(ExampleEndpointController.class);
      super.configure();
    }
  }

}
