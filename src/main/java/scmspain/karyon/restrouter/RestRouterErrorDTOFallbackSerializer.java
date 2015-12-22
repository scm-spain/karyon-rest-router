package scmspain.karyon.restrouter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scmspain.karyon.restrouter.handlers.RestRouterErrorDTO;
import scmspain.karyon.restrouter.serializer.Serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * This Serializer is the last resource to serialize any error but it only knows how to serialize
 * {@link RestRouterErrorDTO}
 */
class RestRouterErrorDTOFallbackSerializer extends Serializer {
  private static final Charset CHARSET = Charset.forName("UTF-8");
  private static final RestRouterErrorDTOFallbackSerializer INSTANCE = new RestRouterErrorDTOFallbackSerializer();
  private static final Logger LOGGER = LoggerFactory.getLogger(RestRouterErrorDTOFallbackSerializer.class);

  public RestRouterErrorDTOFallbackSerializer() {
    super(new String[] {"*/*"});
  }

  @Override
  public void serialize(Object obj, OutputStream outputStream) {
    if (obj instanceof RestRouterErrorDTO) {
      RestRouterErrorDTO errorDTO = (RestRouterErrorDTO)obj;

      try {
        outputStream.write(plainErrorDtoSerialization(errorDTO));
      } catch (IOException e) {
        LOGGER.warn("Cannot serialize error", e);
      }
    }
  }

  private byte[] plainErrorDtoSerialization(RestRouterErrorDTO errorDTO) {
    return ("{\"description\":\"" + errorDTO.getDescription() + "\",\"timestamp\":\"" + errorDTO.getTimestamp() + "\"}")
        .getBytes(CHARSET);
  }

  static RestRouterErrorDTOFallbackSerializer getInstance() {
    return INSTANCE;
  }
}
