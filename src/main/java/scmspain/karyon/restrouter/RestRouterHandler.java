package scmspain.karyon.restrouter;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.apache.commons.lang.StringUtils;
import org.commonjava.mimeparse.MIMEParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.exception.InvalidAcceptHeaderException;
import scmspain.karyon.restrouter.handlers.DefaultKaryonErrorHandler;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.handlers.RestRouterErrorDTO;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.serializer.SerializeWriter;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.Route;
import scmspain.karyon.restrouter.transport.http.RouteNotFound;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RestRouterHandler implements RequestHandler<ByteBuf, ByteBuf> {
  private static final Logger L = LoggerFactory.getLogger(RestRouterHandler.class);
  private SerializeManager serializerManager;
  private RestUriRouter<ByteBuf, ByteBuf> restUriRouter;
  private DefaultKaryonErrorHandler defaultKaryonErrorHandler = new DefaultKaryonErrorHandler();
  private ErrorHandler<ByteBuf> errorHandler;

  /**
   * Creates an instance
   * @param restUriRouter the rest uri router
   * @param serializerManager the serializer manager
   * @param errorHandler the error handler
   */
  @Inject
  public RestRouterHandler(RestUriRouter<ByteBuf, ByteBuf> restUriRouter,
                           SerializeManager serializerManager,
                           ErrorHandler<ByteBuf> errorHandler) {

    this.serializerManager = serializerManager;
    this.restUriRouter = restUriRouter;
    this.errorHandler = errorHandler;
  }

  /**
   * <p>
   *   It handles every request and delegates to the appropriate {@link scmspain.karyon.restrouter.annotation.Endpoint}
   * </p>
   * <p>
   *   It also uses negotiates the response content using accept header and applies the corresponding
   *   serializer according to the {@link scmspain.karyon.restrouter.annotation.Path} annotated
   *   method using the return type of the method.
   * </p>
   * <p>
   *   If the method returns Observable&lt;Void%gt; it will not use the accept negociation, so it will
   *   be the responsibility of the endpoint method to handle it appropriately.
   * </p>
   * <p>
   *   If the method returns some other type of elements in the observable, it will use the content
   *   negotiation and at the end it will try to serialize the DTO returned.
   * </p>
   * @param request the request
   * @param response the response
   * @return an Observable with just onComplete when all the resquest is handled and sent to the
   * response, or onError is something bad, and uncontrolled happens
   */
  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request,
                                 HttpServerResponse<ByteBuf> response) {

    Route<ByteBuf, ByteBuf> route =  restUriRouter.findBestMatch(request, response)
        .orElse(new RouteNotFound<>());
    Observable<Void> result;

    if (route.isCustomSerialization()) {
      result = handleCustomSerialization(route, request, response);

    } else {
      result = handleSupported(route, request, response);

    }

    return result.doOnError(throwable -> L.error("Internal Server Error", throwable));
  }

  private Observable<Void> handleSupported(Route<ByteBuf,ByteBuf> route,
                                          HttpServerRequest<ByteBuf> request,
                                          HttpServerResponse<ByteBuf> response) {
    Observable<Object> resultObs;
    Optional<String> negotiatedContentType;
    Set<String> supportedContentTypes = getSupportedContentTypes(route);

    try {
      negotiatedContentType = Optional.of(acceptNegotiation(request, supportedContentTypes));
      resultObs = route.getHandler().process(request, response);

    } catch (CannotSerializeException | InvalidAcceptHeaderException e) {
      resultObs = Observable.error(e);
      negotiatedContentType = Optional.empty();
    }

    String contentType = negotiatedContentType
        .orElseGet(serializerManager::getDefaultContentType);

    resultObs = resultObs.onErrorResumeNext(throwable -> {
          if (errorHandler != null) {
            return errorHandler.handleError(request, throwable, response::setStatus);

          } else {
            return Observable.error(throwable);
          }
        }
    );

    // If RouteNotFound is not handle it will be handled here
    resultObs = resultObs.onErrorResumeNext(throwable -> {
      return defaultKaryonErrorHandler.handleError(request, throwable, response::setStatus);
    });

    Serializer serializer = serializerManager.getSerializer(contentType)
        .orElseThrow(() -> new CannotSerializeException("Cannot serialize " + contentType));

    SerializeWriter writer = new SerializeWriter(response, contentType);

    return resultObs.flatMap(result -> writer.write(result, serializer));
  }


  private Observable<Void> handleCustomSerialization(Route<ByteBuf, ByteBuf> route,
                                                    HttpServerRequest<ByteBuf> request,
                                                    HttpServerResponse<ByteBuf> response) {

    Observable<Object> resultObs = route.getHandler()
        .process(request, response);

    resultObs = resultObs.onErrorResumeNext(throwable -> {
      return defaultKaryonErrorHandler.handleError(request, throwable, response::setStatus)
          .map(this::serializeErrorDto)
          .flatMap(response::writeStringAndFlush);
    });

    return resultObs.cast(Void.class);
  }

  private String serializeErrorDto(RestRouterErrorDTO errorDTO) {
    return "{\"description\":\"" + errorDTO.getDescription() + "\",\"timestamp\":\"" + errorDTO.getTimestamp() + "\"}";

  }

  private Set<String> getSupportedContentTypes(Route<ByteBuf, ByteBuf> route) {
    Set<String> supportedContentTypes = serializerManager.getSupportedMediaTypes();

    if (!route.getProduces().isEmpty()) {
      supportedContentTypes = route.getProduces();
    }

    return supportedContentTypes;
  }


  private String acceptNegotiation(HttpServerRequest<ByteBuf> request,
                                   Set<String> supportedContentTypes) {

    String accept = request.getHeaders().get(HttpHeaders.ACCEPT);

    if (StringUtils.isBlank(accept)) {
      if (supportedContentTypes.contains(this.serializerManager.getDefaultContentType())) {
        return this.serializerManager.getDefaultContentType();
      }

      switch (supportedContentTypes.size()) {
        case 0:
          throw new CannotSerializeException("Cannot determine the content-type to serialize");

        case 1:
          return supportedContentTypes.stream().findFirst().get();

        default:
          throw new CannotSerializeException("Cannot determine the content-type to serialize between: " + supportedContentTypes);
      }
    } else {
      return getSerializerContentType(accept, supportedContentTypes);
    }

  }

  private String getSerializerContentType(String acceptHeader, Set<String> supportedContentTypes) {
    validateAcceptValue(acceptHeader);


    String serializeContentType = MIMEParse.bestMatch(supportedContentTypes, acceptHeader);
    if (StringUtils.isBlank(serializeContentType)) {
      throw new CannotSerializeException("Cannot serialize with the given content type: " + acceptHeader);
    }

    return serializeContentType;
  }

  private void validateAcceptValue(String acceptHeaderValue) {
    String[] mediaTypesStr = acceptHeaderValue.split(",");

    try {
      Stream.of(mediaTypesStr)
          .map(String::trim)
          .map(MediaType::parse)
          .collect(Collectors.toList());


    } catch (IllegalArgumentException e) {
      throw new InvalidAcceptHeaderException(e);
    }
  }
}

