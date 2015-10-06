package scmspain.karyon.restrouter.serializer;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
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
  private Set<String> supportedMediaTypes;

  public SerializeManager(String defaultContentType, MediaTypeSerializer... serializers) {
    this(defaultContentType, Arrays.asList(serializers));
  }

  public SerializeManager(String defaultContentType, List<MediaTypeSerializer> serializers) {
    this.serializers = ImmutableList.copyOf(serializers);
    this.defaultContentTypeStr = defaultContentType;
    this.defaultContentType = MediaType.parse(defaultContentType);
    this.supportedMediaTypes = serializers.stream()
        .map(MediaTypeSerializer::getMediaTypesStr)
        .flatMap(Arrays::stream)
        .collect(Collectors.toSet());

  }


//  @Override
//  public Observable<Void> intercept(Observable<Object> responseBody,
//                                    HttpServerRequest<ByteBuf> request,
//                                    HttpServerResponse<ByteBuf> response) {
//
//    String targetContentType = getTargetContentType(request.getHeaders());
//    MediaType mediaTypeSerializer;
//
//    try {
//      validateAcceptValue(targetContentType);
//      mediaTypeSerializer = getSerializerContentType(targetContentType);
//
//    } catch(InvalidAcceptHeaderException|CannotSerializeException e) {
//      return Observable.error(e);
//    }
//
//    // TODO: Move to a Map<Content-Type, Serializer>
//    Optional<MediaTypeSerializer> serializerOpt = serializers.stream()
//        .filter(serializer -> serializer.canHandle(mediaTypeSerializer))
//        .findFirst();
//
//    if (serializerOpt.isPresent()) {
//      MediaTypeSerializer serializer = serializerOpt.get();
//
//      response.getHeaders().setHeader(HttpHeaders.CONTENT_TYPE, targetContentType);
//
//      return responseBody
//          .map(serializer::serialize)
//          .flatMap(response::writeBytesAndFlush);
//
//    } else {
//
//      return cannotSerialize(mediaTypeSerializer);
//    }
//
//  }
//
//  private void validateAcceptValue(String acceptHeaderValue) {
//    String[] mediaTypesStr = acceptHeaderValue.split(",");
//
//    try {
//      Stream.of(mediaTypesStr)
//          .map(MediaType::parse)
//          .collect(Collectors.toList());
//
//
//    } catch (IllegalArgumentException e) {
//      throw new InvalidAcceptHeaderException(e);
//    }
//  }
//
//  private String getTargetContentType(HttpRequestHeaders requestHeaders) {
//    return requestHeaders.getHeader(HttpHeaders.ACCEPT, defaultContentTypeStr);
//  }
//
//  private MediaType getSerializerContentType(String targetContentType) {
//    try {
//      String contentType = MIMEParse.bestMatch(supportedMediaTypes, targetContentType);
//
//      return MediaType.parse(contentType);
//
//    } catch(IllegalArgumentException e) {
//      throw new CannotSerializeException(targetContentType, e);
//    }
//
//  }
//
//  private Observable<Void> cannotSerialize(MediaType contentType) {
//    return Observable.error(new CannotSerializeException("Cannot serialize to contentType '"
//            + contentType
//            + "', please register a serializer that can handle this media type."));
//  }

  public static Builder builder() {
    return new Builder();
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

  public static final class Builder {
    private String defaultContentType;
    private List<MediaTypeSerializer> mediaTypeSerializer = new ArrayList<>();

    private Builder(){

    }

    public Builder defaultContentType(String defaultContentType){
      this.defaultContentType = defaultContentType;
      return this;
    }

    public Builder addSerializer(MediaTypeSerializer serializer) {
      mediaTypeSerializer.add(serializer);

      return this;
    }

    public SerializeManager build() {
      return new SerializeManager(defaultContentType, mediaTypeSerializer);
    }

  }
}
