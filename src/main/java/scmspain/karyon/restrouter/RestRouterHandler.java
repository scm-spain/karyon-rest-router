package scmspain.karyon.restrouter;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.apache.commons.lang.StringUtils;
import org.commonjava.mimeparse.MIMEParse;
import rx.Observable;
import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.exception.InvalidAcceptHeaderException;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.serializer.SerializeWriter;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.Route;
import scmspain.karyon.restrouter.transport.http.RouteNotFound;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RestRouterHandler implements RequestHandler<ByteBuf, ByteBuf> {
  private SerializeManager serializerManager;
  private RestUriRouter<ByteBuf, ByteBuf> restUriRouter;

  @Inject
  public RestRouterHandler(RestUriRouter<ByteBuf, ByteBuf> restUriRouter,
                           SerializeManager serializerManager) {

    this.serializerManager = serializerManager;
    this.restUriRouter = restUriRouter;
  }

  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request,
                                 HttpServerResponse<ByteBuf> response) {

    Route<ByteBuf, ByteBuf> route =  restUriRouter.findBestMatch(request, response)
        .orElse(new RouteNotFound<>());

    if (route.isCustom()) {
      return handleCustom(route, request, response);

    } else {
      return handleSupported(route, request, response);

    }
  }

  public Observable<Void> handleSupported(Route<ByteBuf,ByteBuf> route,
                                          HttpServerRequest<ByteBuf> request,
                                          HttpServerResponse<ByteBuf> response) {

    Observable<Object> resultObs;

    String contentType = serializerManager.getDefaultContentType();


    Set<String> supportedContentTypes = getSupportedContentTypes(route);

    try {
      contentType = acceptNegociation(request, supportedContentTypes);

      resultObs = route.getHandler().process(request, response);

    } catch (CannotSerializeException |InvalidAcceptHeaderException e) {
      resultObs = Observable.error(e);
    }

    resultObs = resultObs.onErrorReturn(throwable ->
            serializerManager.getErrorHandler().handleError(throwable,
                response::setStatus
            )
    );


    Serializer serializer = serializerManager.getSerializer(contentType);

    SerializeWriter writer = new SerializeWriter(response, contentType);

    return resultObs.flatMap(result -> writer.write(result, serializer));
  }


  public Observable<Void> handleCustom(Route<ByteBuf,ByteBuf> route,
                                       HttpServerRequest<ByteBuf> request,
                                       HttpServerResponse<ByteBuf> response) {

    // FIXME: generic type insanity
    return (Observable) route.getHandler().process(request, response);
  }

  private Set<String> getSupportedContentTypes(Route<ByteBuf, ByteBuf> route) {
    Set<String> supportedContentTypes = serializerManager.getSupportedMediaTypes();

    if (!route.getProduces().isEmpty()) {
      supportedContentTypes = route.getProduces();
    }

    return supportedContentTypes;
  }


  // TODO: Implement
  private String acceptNegociation(HttpServerRequest<ByteBuf> request,
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
    try {
      validateAcceptValue(acceptHeader);


      String serializeContentType = MIMEParse.bestMatch(supportedContentTypes, acceptHeader);
      if (StringUtils.isBlank(serializeContentType)) {
        throw new CannotSerializeException("Cannot serialize with the given content type: " + acceptHeader);
      }

      return serializeContentType;

    } catch(IllegalArgumentException e) {
      throw new CannotSerializeException(acceptHeader, e);
    }

  }

  private void validateAcceptValue(String acceptHeaderValue) {
    String[] mediaTypesStr = acceptHeaderValue.split(",");

    try {
      Stream.of(mediaTypesStr)
          .map(MediaType::parse)
          .collect(Collectors.toList());


    } catch (IllegalArgumentException e) {
      throw new InvalidAcceptHeaderException(e);
    }
  }
}

