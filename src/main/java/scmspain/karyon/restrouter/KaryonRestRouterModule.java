package scmspain.karyon.restrouter;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.http.KaryonHttpModule;

public abstract class KaryonRestRouterModule extends KaryonHttpModule<ByteBuf, ByteBuf> {

  public KaryonRestRouterModule() {
    super("karyonRestModule", ByteBuf.class, ByteBuf.class);
  }

  protected KaryonRestRouterModule(String moduleName) {
    super(moduleName, ByteBuf.class, ByteBuf.class);
  }

  @Override
  protected void configure() {
    bindRouter().to(RestBasedRouter.class);
    super.configure();
  }

}
