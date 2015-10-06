package scmspain.karyon.restrouter;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.http.KaryonHttpModule;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.RouteFilterChain;
import scmspain.karyon.restrouter.transport.http.RouteInterceptorSupport;

public abstract class KaryonRestRouterModule extends KaryonHttpModule<ByteBuf, ByteBuf> {

  private RouteInterceptorSupport routeInterceptorSupport = new RouteInterceptorSupport();

  public KaryonRestRouterModule() {
    super("karyonRestModule", ByteBuf.class, ByteBuf.class);
  }

  protected KaryonRestRouterModule(String moduleName) {
    super(moduleName, ByteBuf.class, ByteBuf.class);
  }

  public void addRouteInterceptor(RouteFilterChain routeFilterChain) {
    routeInterceptorSupport.addOutInterceptor(routeFilterChain);
  }


  @Override
  protected void configure() {
    bind(RestUriRouter.class);
    bind(RestRouterScanner.class);

    bindRouter().to(RestRouterHandler.class);
    bind(RouteInterceptorSupport.class).toInstance(routeInterceptorSupport);
    super.configure();
  }

}
