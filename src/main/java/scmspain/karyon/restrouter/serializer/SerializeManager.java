package scmspain.karyon.restrouter.serializer;

import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.handlers.ErrorHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SerializeManager {

  private Map<String, Serializer> serializers;
  private String defaultContentType;

  private ErrorHandler errorHandler;

  public SerializeManager(Configuration configuration) {
    setSerializers(configuration.getSerializers());
    this.defaultContentType = configuration.getDefaultContentType();
    this.errorHandler = configuration.getErrorHandler();

    validate();
  }

  private void validate() {

  }


  private void setSerializers(List<Serializer> serializers) {
    this.serializers = new HashMap<>();
    for (Serializer serializer: serializers) {
      for (String mediaType: serializer.getMediaTypes()) {
        this.serializers.put(mediaType, serializer);
      }
    }
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public Set<String> getSupportedMediaTypes() {
    return serializers.keySet();
  }

  public String getDefaultContentType() {
    return defaultContentType;
  }

  public Serializer getSerializer(String contentType) {
    Serializer serializer = serializers.get(contentType);

    if(serializer == null) {
      throw new CannotSerializeException("Cannot serialize " + contentType);
    }

    return serializer;
  }

}
