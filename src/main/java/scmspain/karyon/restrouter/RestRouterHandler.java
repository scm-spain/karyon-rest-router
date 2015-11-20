package scmspain.karyon.restrouter;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
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
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.handlers.KaryonRestRouterErrorHandler;
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
  private SerializeManager serializerManager;
  private RestUriRouter<ByteBuf, ByteBuf> restUriRouter;
  private KaryonRestRouterErrorHandler karyonRestRouterErrorHandler = new KaryonRestRouterErrorHandler();
  private ErrorHandler<ByteBuf> errorHandler;

  private static final Logger L = LoggerFactory.getLogger(RestRouterHandler.class);

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

    if (route.hasCustomSerialization() || !serializerManager.hasSerializers()) {
      result = handleCustomSerialization(route, request, response);

    } else {
      result = handleSupported(route, request, response);

    }

    return result.onErrorResumeNext(throwable -> {
      response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

      logError(throwable, request);

      response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "text/plain");
      return response.writeStringAndFlush("Internal server error");
    });
  }

  private void logError(Throwable throwable, HttpServerRequest<ByteBuf> request) {
    try {
      HttpMethod method = request.getHttpMethod();
      String methodStr = method != null ? method.name() : "<Unknown>";
      String path = request.getPath() + "?" + request.getQueryString();

      String requestHeaders = getRequestHeaders(request);

      String message = String.format("Internal server error requesting [%s %s] [HEADERS=> %s]", methodStr, path, requestHeaders);

      L.error(message, throwable);
    } catch (Throwable t) {
      // Just to be sure we don't generate additional errors
      L.error("Internal server error", throwable);
    }
  }

  private String getRequestHeaders(HttpServerRequest<ByteBuf> request) {
    return request.getHeaders().entries().stream()
        .map(entry -> entry.getKey() + ": " + entry.getValue())
        .reduce((entry1, entry2) -> entry1 + ", " + entry2)
        .orElse("");
  }

  private Observable<Void> handleSupported(Route<ByteBuf,ByteBuf> route,
                                          HttpServerRequest<ByteBuf> request,
                                          HttpServerResponse<ByteBuf> response) {
    Set<String> supportedContentTypes = getSupportedContentTypes(route);
    String accept = request.getHeaders().get(HttpHeaders.ACCEPT);
    Optional<String> negotiatedContentType;
    String contentType;
    Observable<Object> resultObs;

    try {
      validateAcceptHeader(accept);

      negotiatedContentType = acceptNegotiation(accept, supportedContentTypes);

      if(negotiatedContentType.isPresent()) {
        resultObs = route.getHandler().process(request, response);
      } else {
        resultObs = Observable.error(
            new CannotSerializeException("Supported content types " + supportedContentTypes)
        );
      }

    } catch (InvalidAcceptHeaderException e) {
      resultObs = Observable.error(e);
      negotiatedContentType = Optional.empty();
    }

    contentType = negotiatedContentType.orElseGet(serializerManager::getDefaultContentType);

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
      return karyonRestRouterErrorHandler.handleError(request, throwable, response::setStatus);
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

      return karyonRestRouterErrorHandler.handleError(request, throwable, response::setStatus)
          .map(this::serializeErrorDto)
          .flatMap(response::writeStringAndFlush);
    });

    return resultObs.cast(Void.class);
  }

  private String plainErrorDtoSerialization(RestRouterErrorDTO errorDTO) {
    return "{\"description\":\"" + errorDTO.getDescription() + "\",\"timestamp\":\"" + errorDTO.getTimestamp() + "\"}";

  }

  private Set<String> getSupportedContentTypes(Route<ByteBuf, ByteBuf> route) {
    Set<String> supportedContentTypes = serializerManager.getSupportedMediaTypes();

    if (!route.getProduces().isEmpty()) {
      supportedContentTypes = route.getProduces();
    }

    return supportedContentTypes;
  }


  private Optional<String> acceptNegotiation(String accept,
                                   Set<String> supportedContentTypes) {

    if (StringUtils.isBlank(accept)) {
      if (supportedContentTypes.contains(this.serializerManager.getDefaultContentType())) {
        return Optional.of(serializerManager.getDefaultContentType());
      }

      // If only one is supported we will use this
      if (supportedContentTypes.size() == 1) {
        return supportedContentTypes.stream().findFirst();
      } else {
        // Otherwise we cannot choose objectively which
        return Optional.empty();
      }

    } else {
      return getSerializerContentType(accept, supportedContentTypes);
    }

  }

  private Optional<String> getSerializerContentType(String acceptHeader, Set<String> supportedContentTypes) {
    String serializeContentType = MIMEParse.bestMatch(supportedContentTypes, acceptHeader);
    if (StringUtils.isBlank(serializeContentType)) {
      return Optional.empty();
    }

    return Optional.of(serializeContentType);
  }

  private void validateAcceptHeader(String acceptHeaderValue) {
    if(acceptHeaderValue != null) {
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
}

