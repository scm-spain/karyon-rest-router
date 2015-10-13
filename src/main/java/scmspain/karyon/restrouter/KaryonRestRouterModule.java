package scmspain.karyon.restrouter;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.http.KaryonHttpModule;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.serializer.Configuration;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;

public abstract class KaryonRestRouterModule extends KaryonHttpModule<ByteBuf, ByteBuf> {

  private SerializeManager serializeManager = new SerializeManager();

  public KaryonRestRouterModule() {
    super("karyonRestModule", ByteBuf.class, ByteBuf.class);
  }

  protected KaryonRestRouterModule(String moduleName) {
    super(moduleName, ByteBuf.class, ByteBuf.class);
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

    bindRouter().to(RestRouterHandler.class);
    super.configure();
  }

}
