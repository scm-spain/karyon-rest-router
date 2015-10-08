package scmspain.karyon.restrouter.serializer;

import org.apache.commons.lang.StringUtils;

import java.io.OutputStream;
import java.util.stream.Stream;

public abstract class Serializer {
  private String[] mediaTypes;

  public Serializer(String[] mediaTypes) {
    this.mediaTypes = mediaTypes;
  }

  public boolean canHandle(String needMediaType) {
    return StringUtils.isNotBlank(needMediaType)
        && Stream.of(mediaTypes).anyMatch(needMediaType::equals);
  }

  public String[] getMediaTypes() {
    return mediaTypes;
  }

  public abstract void serialize(Object obj, OutputStream outputStream);
}
