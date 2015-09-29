package scmspain.karyon.restrouter;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.http.KaryonHttpModule;
import scmspain.karyon.restrouter.transport.http.RouteOutInterceptor;
import scmspain.karyon.restrouter.transport.http.RouteInterceptorSupport;

public abstract class KaryonRestRouterModule extends KaryonHttpModule<ByteBuf, ByteBuf> {

  private RouteInterceptorSupport routeInterceptorSupport = new RouteInterceptorSupport();

  public KaryonRestRouterModule() {
    super("karyonRestModule", ByteBuf.class, ByteBuf.class);
  }

  protected KaryonRestRouterModule(String moduleName) {
    super(moduleName, ByteBuf.class, ByteBuf.class);
  }

  public void addRouteInterceptor(RouteOutInterceptor routeOutInterceptor) {
    routeInterceptorSupport.addOutInterceptor(routeOutInterceptor);
  }


  @Override
  protected void configure() {
    bindRouter().to(RestBasedRouter.class);
    bind(RouteInterceptorSupport.class).toInstance(routeInterceptorSupport);
    super.configure();
  }

}
