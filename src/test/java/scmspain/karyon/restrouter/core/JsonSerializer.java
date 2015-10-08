package scmspain.karyon.restrouter.core;

import com.google.common.net.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import scmspain.karyon.restrouter.serializer.MediaTypeSerializer;
import scmspain.karyon.restrouter.serializer.SerializeWriter;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Created by borja.vazquez on 8/10/15.
 */
public class JsonSerializer extends MediaTypeSerializer {
  public JsonSerializer() {
    super(
        Stream.of("application/json").toArray(String[]::new),
        Stream.of("application/json").toArray(String[]::new));
  }

  @Override
  public void serialize(Object obj, SerializeWriter serializeWriter) {
    try {
      if (obj == null) {
        throw new RuntimeException("Object to serialize cannot be null");
      }

      ObjectMapper mapper = new ObjectMapper();
      byte[] serializedObj = mapper.writeValueAsBytes(obj);
      serializeWriter.write(serializedObj);
    } catch (IOException e) {
      throw new RuntimeException("Error serializing the handler return value: " + obj, e);
    }
  }
}
