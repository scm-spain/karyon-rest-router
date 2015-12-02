package scmspain.karyon.restrouter;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.http.KaryonHttpModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.serializer.Configuration;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;

import java.util.Arrays;
import java.util.List;

public abstract class KaryonRestRouterModule extends KaryonHttpModule<ByteBuf, ByteBuf> {
  private static final Logger LOGGER = LoggerFactory.getLogger(KaryonRestRouterModule.class);

  private Configuration configuration;
  private boolean configurationConsumed = false;

  public KaryonRestRouterModule() {
    super("karyonRestModule", ByteBuf.class, ByteBuf.class);
  }

  protected KaryonRestRouterModule(String moduleName) {
    super(moduleName, ByteBuf.class, ByteBuf.class);
  }

  public void setConfiguration(Configuration configuration) {
    if(configurationConsumed) {
      throw new RuntimeException("Configuration already consumed, set configuration before call KaryonRestRouterModule.configure()");
    }

    this.configuration = configuration;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  protected void configure() {
    bind(RestUriRouter.class);

    if(configuration == null) {
      setDefaultConfiguration();
    }

    logConfiguration();

    bind(SerializeManager.class).toInstance(new SerializeManager(configuration.getSerializers(), configuration.getDefaultContentType()));
    configurationConsumed = true;

    bind(RestRouterScanner.class);

    Provider<ErrorHandler<ByteBuf>> errorHandlerProvider = configuration::getErrorHandler;

    bind(new TypeLiteral<ErrorHandler<ByteBuf>>() {
    })
        .toProvider(errorHandlerProvider);

    bindRouter().to(RestRouterHandler.class);

    super.configure();
  }

  private void logConfiguration() {
    logSerializers();
    logErrorHandler();

  }

  private void logSerializers() {
    List<Serializer> serializers = configuration.getSerializers();

    if(serializers.isEmpty()) {
      LOGGER.info("Serializer configured: <none>");
    } else {
      configuration.getSerializers().forEach(serializer -> {
        LOGGER.info(String.format("Serialized configured: %s for MediaTypes '%s'", serializer.getClass(), Arrays.asList(serializer.getMediaTypes())));
      });
    }
  }

  private void logErrorHandler() {
    LOGGER.info("ErrorHandler configured: " + configuration.getErrorHandler().getClass());
  }


  private void setDefaultConfiguration() {
    LOGGER.warn("No configuration found, default configuration is set");
    this.configuration = Configuration.builder().build();
  }

}

