package scmspain.karyon.restrouter;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.http.KaryonHttpModule;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.handlers.ErrorHandlerImpl;
import scmspain.karyon.restrouter.serializer.MediaTypeSerializer;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.RouteFilterChain;
import scmspain.karyon.restrouter.transport.http.RouteInterceptorSupport;

public abstract class KaryonRestRouterModule extends KaryonHttpModule<ByteBuf, ByteBuf> {

  private RouteInterceptorSupport routeInterceptorSupport = new RouteInterceptorSupport();
  private SerializeManager serializeManager = new SerializeManager();

  public KaryonRestRouterModule() {
    super("karyonRestModule", ByteBuf.class, ByteBuf.class);
  }

  protected KaryonRestRouterModule(String moduleName) {
    super(moduleName, ByteBuf.class, ByteBuf.class);
  }


  public void setDefaultContentType(String defaultContentType){
    this.serializeManager.setDefaultContentType(defaultContentType);
  }

  public void addSerializers(MediaTypeSerializer... serializers) {
    serializeManager.setSerializers(serializers);
  }


  @Override
  protected void configure() {
    bind(RestUriRouter.class);
    bind(ErrorHandler.class).to(ErrorHandlerImpl.class);
    bind(SerializeManager.class).toInstance(serializeManager);
    bind(RestRouterScanner.class);

    bindRouter().to(RestRouterHandler.class);
    bind(RouteInterceptorSupport.class).toInstance(routeInterceptorSupport);
    super.configure();
  }

}
