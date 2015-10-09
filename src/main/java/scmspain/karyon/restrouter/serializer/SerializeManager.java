package scmspain.karyon.restrouter.serializer;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.handlers.ErrorHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SerializeManager {
  private List<Serializer> serializers;
  private String defaultContentType;
  private Set<String> supportedMediaTypes = new HashSet<>();
  private ErrorHandler errorHandler;


  public void setDefaultContentType(String defaultContentType) {
    this.defaultContentType = defaultContentType;
  }

  public void setSerializers(Serializer... serializers) {
    setSerializers(Arrays.asList(serializers));
  }

  public void setSerializers(List<Serializer> serializers) {
    this.serializers = ImmutableList.copyOf(serializers);

    this.supportedMediaTypes = serializers.stream()
        .map(Serializer::getMediaTypes)
        .flatMap(Arrays::stream)
        .collect(Collectors.toSet());
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public Set<String> getSupportedMediaTypes() {
    return supportedMediaTypes;
  }

  public String getDefaultContentType() {
    return defaultContentType;
  }

  public Serializer getSerializer(String contentType) {
    return serializers.stream()
        .filter(serializer -> serializer.canHandle(contentType))
        .findFirst().orElseThrow(() -> new CannotSerializeException("Cannot serialize " + contentType));
  }

}
