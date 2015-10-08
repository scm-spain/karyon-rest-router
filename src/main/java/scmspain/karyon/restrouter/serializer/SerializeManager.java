package scmspain.karyon.restrouter.serializer;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
Add tests to all library. Remember the accept header can have multiple types
Remove jackson, as this is just an API to have more formats
Create a nice builder to use this interceptor
Commit
Create modules of this project foreach format it allow, currently it should support all formats
*/
public class SerializeManager {
  private List<MediaTypeSerializer> serializers;

  private String defaultContentTypeStr;
  private MediaType defaultContentType;
  private Set<String> supportedMediaTypes = new HashSet<>();

  public void setDefaultContentType(String defaultContentType) {
    this.defaultContentTypeStr = defaultContentType;
    this.defaultContentType = MediaType.parse(defaultContentType);
  }

  public void setSerializers(MediaTypeSerializer... serializers) {
    setSerializers(Arrays.asList(serializers));
  }

  public void setSerializers(List<MediaTypeSerializer> serializers) {
    this.serializers = ImmutableList.copyOf(serializers);

    this.supportedMediaTypes = serializers.stream()
        .map(MediaTypeSerializer::getMediaTypesStr)
        .flatMap(Arrays::stream)
        .collect(Collectors.toSet());
  }



  public Set<String> getSupportedMediaTypes() {
    return supportedMediaTypes;
  }

  public String getDefaultContentType() {
    return defaultContentTypeStr;
  }

  public Serializer getSerializer(String contentType) {
    return null;
  }

}
