package scmspain.karyon.restrouter.core;

import com.google.inject.Singleton;
import com.netflix.governator.annotations.Modules;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.archaius.ArchaiusBootstrap;
import rx.Observable;
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
      this.addRouteInterceptor((result, request, response) -> {
        Observable<Integer> integerObs = (Observable) result;
        return integerObs.map(i -> i * 5);
      });
      this.addRouteInterceptor((result, request, response) -> {
        Observable<Integer> integerObs = (Observable) result;
        return integerObs.map(i -> i + 1);
      });
      this.addRouteInterceptor((result, request, response) -> {
        Observable<Integer> integerObs = (Observable) result;
        return integerObs.map(i -> i/2);
      });
      this.addRouteInterceptor(new TestJsonRouteInterceptor());
    }

    @Override
    public void configure() {
      bind(ExampleEndpointController.class);
      super.configure();
    }
  }
}
