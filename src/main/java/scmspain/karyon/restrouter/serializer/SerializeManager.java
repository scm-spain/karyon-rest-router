package scmspain.karyon.restrouter.serializer;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import scmspain.karyon.restrouter.exception.CannotSerializeException;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SerializeManager {
  private Map<String, Serializer> serializers;
  private String defaultContentType;

  public void setDefaultContentType(String defaultContentType) {
    this.defaultContentType = defaultContentType;
  }

  public void setSerializers(Serializer... serializers) {
    setSerializers(Arrays.asList(serializers));
  }

  public void setSerializers(List<Serializer> serializers) {
    this.serializers = new HashMap<>();
    for (Serializer serializer: serializers) {
      for (String mediaType: serializer.getMediaTypes()) {
        this.serializers.put(mediaType, serializer);
      }
    }
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
