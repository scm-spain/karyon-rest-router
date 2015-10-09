package scmspain.karyon.restrouter;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.http.KaryonHttpModule;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.serializer.Configuration;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
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

  public void addSerializers(Serializer... serializers) {
    this.serializeManager.setSerializers(serializers);
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.serializeManager.setErrorHandler(errorHandler);
  }

  public void setConfiguration(Configuration configuration) {
    this.serializeManager.setDefaultContentType(configuration.getDefaultContentType());
    this.serializeManager.setSerializers(configuration.getSerializers());
    this.serializeManager.setErrorHandler(configuration.getErrorHandler());
  }

  @Override
  protected void configure() {
    bind(RestUriRouter.class);
    bind(SerializeManager.class).toInstance(serializeManager);
    bind(RestRouterScanner.class);
    bind(RouteInterceptorSupport.class).toInstance(routeInterceptorSupport);

    bindRouter().to(RestRouterHandler.class);
    super.configure();
  }

}
