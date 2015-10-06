package scmspain.karyon.restrouter;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;
import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.exception.InvalidAcceptHeaderException;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.serializer.SerializeWriter;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.Route;

import javax.inject.Inject;
import java.util.Set;

public class RestRouterHandler implements RequestHandler<ByteBuf, ByteBuf> {
  private ErrorHandler errorHandler;
  private SerializeManager serializerManager;
  private RestUriRouter<ByteBuf, ByteBuf> restUriRouter;

  @Inject
  public RestRouterHandler(RestUriRouter<ByteBuf, ByteBuf> restUriRouter, ErrorHandler errorHandler, SerializeManager serializerManager) {
    this.errorHandler = errorHandler;
    this.serializerManager = serializerManager;
    this.restUriRouter = restUriRouter;
  }

  @Override
  public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    Observable<Object> resultObs;
    Set<String> supportedContentTypes = serializerManager.getSupportedMediaTypes();
    String contentType = serializerManager.getDefaultContentType();

    Route<ByteBuf, ByteBuf> route =  restUriRouter.findBestMatch(request, response)
        .orElse(new RouteNotFound<>());

    boolean negociateAccept = route.isBasedOnSerializers();

    try {
      if (negociateAccept) {
        contentType = acceptNegociation(supportedContentTypes);
      }
      resultObs = route.getHandler().process(request, response);

    } catch (CannotSerializeException |InvalidAcceptHeaderException e) {
      resultObs = Observable.error(e);
    }

    resultObs = resultObs.onErrorReturn(throwable ->
      errorHandler.handleError(throwable,
          /*canSerializeError = */ negociateAccept,
          response::setStatus
      )
    );

    if (negociateAccept) {
      Serializer serializer = serializerManager.getSerializer(contentType);

      SerializeWriter writer = new SerializeWriter(response, contentType);

      return resultObs.map(result -> {
        serializer.serialize(result, writer);
        return null;
      });

    } else {
      // FIXME: Generic type checking
      return (Observable)resultObs;
    }
  }

  class RouteNotFound<I,O> extends Route<I,O> {
    public RouteNotFound() {
      super((request, context) -> true, new RouteNotFoundHandler<>());
    }

    @Override
    public boolean isBasedOnSerializers() {
      return true;
    }
  }

  // TODO: Implement
  private String acceptNegociation(Set<String> supportedContentTypes) {
    return null;
  }

}
