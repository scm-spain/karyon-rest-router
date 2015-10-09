package scmspain.karyon.restrouter.serializer;

import scmspain.karyon.restrouter.handlers.ErrorHandler;

import java.util.ArrayList;
import java.util.List;


public class Configuration {

  private String defaultContentType;
  private List<Serializer> serializers;
  private ErrorHandler errorHandler;

  public Configuration(String defaultContentType, List<Serializer> serializers, ErrorHandler errorHandler) {
    this.defaultContentType = defaultContentType;
    this.serializers = serializers;
    this.errorHandler = errorHandler;
  }

  public String getDefaultContentType() {
    return this.defaultContentType;
  }

  public List<Serializer> getSerializers() {
    return this.serializers;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String defaultContentType;
    private List<Serializer> serializers = new ArrayList<>();
    private ErrorHandler errorHandler;

    private Builder(){
    }

    public Builder defaultContentType(String defaultContentType){
      this.defaultContentType = defaultContentType;
      return this;
    }

    public Builder addSerializer(Serializer serializer) {
      serializers.add(serializer);
      return this;
    }

    public Builder errorHandler(ErrorHandler errorHandler) {
      this.errorHandler = errorHandler;
      return this;
    }

    public Configuration build() {
      return new Configuration(defaultContentType, serializers, errorHandler);
    }

  }
}
