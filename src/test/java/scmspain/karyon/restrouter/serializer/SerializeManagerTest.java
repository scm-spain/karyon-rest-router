package scmspain.karyon.restrouter.serializer;

import org.junit.Test;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pablo.diaz on 19/11/15.
 */
public class SerializeManagerTest {



  @Test
  public void givenASerializerManagerWithSerializersThatSupportsDefaultContentTypeWhenValidateItShouldValidate() throws Exception {
    List<Serializer> serializers = Collections.singletonList(createSerializer("application/json"));

    SerializeManager serializeManager = new SerializeManager(serializers, "application/json");

    serializeManager.validate();
  }


  @Test(expected = RuntimeException.class)
  public void givenASerializerManagerWithSerializersThatDoesntSupportsDefaultContentTypeWhenValidateItShouldNotValidate() throws Exception {
    List<Serializer> serializers = Collections.singletonList(createSerializer("application/json"));

    SerializeManager serializeManager = new SerializeManager(serializers, "default_not_supported");

    serializeManager.validate();
  }

  @Test
  public void givenASerializerManagerWithoutSerializersWhenValidateItShouldValidateCorrectly() throws Exception {
    List<Serializer> serializers = Collections.emptyList();

    SerializeManager serializeManager = new SerializeManager(serializers, "whatever");

    serializeManager.validate();
  }

  private Serializer createSerializer(String... contentTypes) {
    return new Serializer(contentTypes) {
      @Override
      public void serialize(Object obj, OutputStream outputStream) {

      }
    };
  }
}