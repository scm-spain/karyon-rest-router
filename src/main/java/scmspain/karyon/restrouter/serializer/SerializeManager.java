package scmspain.karyon.restrouter.serializer;

import com.google.common.base.Preconditions;
import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.handlers.ErrorHandler;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manage the serializers
 */
public class SerializeManager {

  private Map<String, Serializer> serializers;
  private String defaultContentType;

  /**
   * Creates a serializer manager
   * @param serializers the list of serializers
   * @param defaultContentType the default content type in case the request doesn't include an Accept header
   */
  public SerializeManager(List<Serializer> serializers, String defaultContentType) {
    Preconditions.checkNotNull(serializers, "Serializers should not be null");

    setSerializers(serializers);
    this.defaultContentType = defaultContentType;
  }

  @Inject
  void validate() {
    if(hasSerializers()) {
      getSerializer(defaultContentType)
          .orElseThrow(() ->
              new RuntimeException("There is no serializer configured for the default content type")
          );
    }
  }

  private void setSerializers(List<Serializer> serializers) {
    this.serializers = new HashMap<>();
    for (Serializer serializer: serializers) {
      for (String mediaType: serializer.getMediaTypes()) {
        this.serializers.put(mediaType, serializer);
      }
    }
  }

  /**
   * @return the suported media types set
   */
  public Set<String> getSupportedMediaTypes() {
    return serializers.keySet();
  }

  /**
   * @return the default content type used in case of Accept header is not send
   */
  public String getDefaultContentType() {
    return defaultContentType;
  }

  /**
   * Retrieves the serializer for a content type
   * @param contentType the content type
   * @return the serializer of the content type requested or {@link CannotSerializeException}
   * if the content Type is not supported
   */
  public Optional<Serializer> getSerializer(String contentType) {
    return Optional.ofNullable(serializers.get(contentType));
  }

  public boolean hasSerializers() {
    return !serializers.isEmpty();
  }

}
