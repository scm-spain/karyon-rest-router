package scmspain.karyon.restrouter.handlers;

import com.google.common.net.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.exception.InvalidAcceptHeaderException;
import scmspain.karyon.restrouter.exception.ParamAnnotationException;
import scmspain.karyon.restrouter.exception.RouteNotFoundException;
import scmspain.karyon.restrouter.exception.UnsupportedFormatException;

/**
 * Default error handler, this is the error generated when the defined {@link ErrorHandler} cannot
 * handle the generated exception
 */
public class DefaultKaryonErrorHandler implements ErrorHandler<ByteBuf> {
  private static final Logger L = LoggerFactory.getLogger(DefaultKaryonErrorHandler.class);

  @Override
  public Observable<RestRouterErrorDTO> handleError(
      HttpServerRequest<ByteBuf> request,
      Throwable throwable,
      StatusCodeSetter statusCode) {


    if (throwable instanceof RouteNotFoundException) {
      statusCode.set(HttpResponseStatus.NOT_FOUND);

      return Observable.just(new RestRouterErrorDTO("Path: " + request.getPath() + " NOT FOUND"));

    }  else if (throwable instanceof ParamAnnotationException) {
      statusCode.set(HttpResponseStatus.BAD_REQUEST);

      return Observable.empty();

    } else if (throwable instanceof UnsupportedFormatException) {
      statusCode.set(HttpResponseStatus.BAD_REQUEST);

      return Observable.just(
          new RestRouterErrorDTO("Impossible to resolve params for the path:" + request.getPath()));

    } else if (throwable instanceof InvalidAcceptHeaderException) {
      statusCode.set(HttpResponseStatus.NOT_ACCEPTABLE);

      return Observable.just(
          new RestRouterErrorDTO("Invalid accept header: " + request.getHeaders().get(HttpHeaders.ACCEPT)));

    } else if (throwable instanceof CannotSerializeException) {
      statusCode.set(HttpResponseStatus.NOT_ACCEPTABLE);

      return Observable.just(
          new RestRouterErrorDTO("Cannot serialize the response in the given accept: " + request.getHeaders().get(HttpHeaders.ACCEPT)));

    } else {
      statusCode.set(HttpResponseStatus.INTERNAL_SERVER_ERROR);

      L.error("Internal server error", throwable);

      return Observable.error(throwable);
    }
  }

}
