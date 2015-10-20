package scmspain.karyon.restrouter.serializer;

import io.netty.buffer.ByteBuf;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.handlers.VoidErrorHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Karyon rest router configuration class
 */
public class Configuration {
  private String defaultContentType;
  private List<Serializer> serializers;
  private ErrorHandler<ByteBuf> errorHandler;

  private Configuration(String defaultContentType, List<Serializer> serializers, ErrorHandler<ByteBuf> errorHandler) {
    this.defaultContentType = defaultContentType;
    this.serializers = serializers;
    this.errorHandler = errorHandler;
  }

  /**
   * @return the default content type
   */
  public String getDefaultContentType() {
    return this.defaultContentType;
  }

  /**
   * @return the list of serializers it can't be null
   */
  public List<Serializer> getSerializers() {
    return this.serializers;
  }

  /**
   * @return the error handler if configured, null otherwise
   */
  public ErrorHandler<ByteBuf> getErrorHandler() {
    return errorHandler;
  }

  /**
   * Adds a serializer to the serializers list
   * @param serializer the serializer to add
   */
  public void addSerializer(Serializer serializer) {
    serializers.add(serializer);
  }

  /**
   * @return A new builder instance to create a Configuration
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Sets the default content type that will be used if the request doesn't send an Accept header
   * @param defaultContentType the default content type
   */
  public void setDefaultContentType(String defaultContentType) {
    this.defaultContentType = defaultContentType;
  }

  /**
   * Sets the list of serializers overriding the existing ones
   * @param serializers the list of serializers
   */
  public void setSerializers(List<Serializer> serializers) {
    this.serializers = serializers;
  }

  /**
   * Sets the error handler to use. It can be null if no error handler is needed
   * @param errorHandler the error handler
   */
  public void setErrorHandler(ErrorHandler<ByteBuf> errorHandler) {
    this.errorHandler = errorHandler;
  }

  /**
   * The configuration builder
   */
  public static final class Builder {
    private String defaultContentType;
    private List<Serializer> serializers = new ArrayList<>();
    private ErrorHandler<ByteBuf> errorHandler = VoidErrorHandler.getInstance();

    private Builder(){
    }

    /**
     * {@link Configuration#setDefaultContentType}
     * @param defaultContentType the default content type
     * @return the same builder
     */
    public Builder defaultContentType(String defaultContentType){
      this.defaultContentType = defaultContentType;
      return this;
    }

    /**
     * {@link Configuration#addSerializer}
     * @param serializer a new serializer to add
     * @return the same builder
     */
    public Builder addSerializer(Serializer serializer) {
      serializers.add(serializer);
      return this;
    }

    /**
     * {@link Configuration#setErrorHandler}
     * @param errorHandler the error handler
     * @return the same builder
     */
    public Builder errorHandler(ErrorHandler<ByteBuf> errorHandler) {
      this.errorHandler = errorHandler;
      return this;
    }

    /**
     * It builds the Configuration with all the data
     * @return a new configuration instance
     */
    public Configuration build() {
      return new Configuration(defaultContentType, serializers, errorHandler);
    }

  }
}
