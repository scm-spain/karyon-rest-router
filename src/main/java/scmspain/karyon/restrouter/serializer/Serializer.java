package scmspain.karyon.restrouter.serializer;

import org.apache.commons.lang.StringUtils;

import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * The serializer base class
 */
public abstract class Serializer {
  private String[] mediaTypes;

  /**
   * Creates a serializer for the given media types
   * @param mediaTypes the media types
   */
  public Serializer(String[] mediaTypes) {
    this.mediaTypes = mediaTypes;
  }

  /**
   * @param needMediaType the media type to check
   * @return true if this serializer can handle that media type
   */
  public boolean canHandle(String needMediaType) {
    return StringUtils.isNotBlank(needMediaType)
        && Stream.of(mediaTypes).anyMatch(needMediaType::equals);
  }

  /**
   * @return the list of media types allowed
   */
  public String[] getMediaTypes() {
    return mediaTypes;
  }

  /**
   * Serializes an object
   * @param obj the object to serialize
   * @param outputStream the stream to send the serialization
   */
  public abstract void serialize(Object obj, OutputStream outputStream);
}
