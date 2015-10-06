package scmspain.karyon.restrouter.serializer;

import com.google.common.collect.ObjectArrays;
import com.google.common.net.MediaType;

import java.util.stream.Stream;

/**
 * Created by pablo.diaz on 29/9/15.
 */
public abstract class MediaTypeSerializer implements Serializer {
  private MediaType[] mediaTypes;
  private String[] mediaTypesStr;

  public MediaTypeSerializer(String[] defaultMediaTypes, String[] mediaTypes) {
    this(ObjectArrays.concat(defaultMediaTypes, mediaTypes, String.class));
  }

  public MediaTypeSerializer(MediaType[] defaultMediaTypes, MediaType[] mediaTypes) {
    this(ObjectArrays.concat(defaultMediaTypes, mediaTypes, MediaType.class));
  }

  public MediaTypeSerializer(String[] mediaTypesStr) {
    this(Stream.of(mediaTypesStr).map(MediaType::parse).toArray(MediaType[]::new), mediaTypesStr);
  }

  public MediaTypeSerializer(MediaType[] mediaTypes) {
    this(mediaTypes, Stream.of(mediaTypes).map(MediaType::toString).toArray(String[]::new));
  }

  public MediaTypeSerializer(MediaType[] mediaTypes, String[] mediaTypesStr) {
    this.mediaTypes = mediaTypes;
    this.mediaTypesStr = mediaTypesStr;
  }

  public boolean canHandle(MediaType neededMediaType) {
    return Stream.of(mediaTypes)
        .anyMatch(neededMediaType::is);
  }

  public MediaType[] getMediaTypes() {
    return mediaTypes;
  }

  public String[] getMediaTypesStr() {
    return mediaTypesStr;
  }
}
