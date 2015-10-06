package scmspain.karyon.restrouter;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpRequestHeaders;
import io.reactivex.netty.protocol.http.server.HttpResponseHeaders;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import rx.Observable;
import rx.observers.TestSubscriber;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.Route;
import scmspain.karyon.restrouter.transport.http.RouteHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by pablo.diaz on 6/10/15.
 */
public class RestRouterHandlerTest {
  @Mock
  private RestUriRouter<ByteBuf, ByteBuf> restUriRouter;
  @Mock
  private ErrorHandler errorHandler;
  @Mock
  private SerializeManager serializerManager;
  @Mock
  private HttpServerRequest<ByteBuf> request;
  @Mock
  private HttpServerResponse<ByteBuf> response;
  @Mock
  private Route<ByteBuf, ByteBuf> route;
  @Mock
  private RouteHandler<ByteBuf, ByteBuf> routeHandler;
  @Mock
  private HttpRequestHeaders requestHeaders;
  @Mock
  private HttpResponseHeaders responseHeaders;
  @Mock
  private Serializer serializer;
  @Mock
  private Object resultBody;

  @Before
  public void setUp() {
    initMocks(this);

    given(request.getHeaders())
        .willReturn(requestHeaders);

    given(response.getHeaders())
        .willReturn(responseHeaders);

    given(restUriRouter.findBestMatch(request, response))
        .willReturn(Optional.of(route));

    given(route.getHandler())
        .willReturn(routeHandler);

    given(serializerManager.getDefaultContentType())
        .willReturn("application/json");

    given(routeHandler.process(request, response))
        .willReturn(Observable.just(resultBody));

  }

  private void setEnv(String accept, Set<String> supported, List<String> produces, boolean isCustom) {
    if (accept != null) {
      given(request.getHeaders().get(HttpHeaders.ACCEPT)).willReturn(accept);
    }
    given(serializerManager.getSupportedMediaTypes())
        .willReturn(supported);

    given(route.isCustom())
        .willReturn(isCustom);

    given(route.getProduces())
        .willReturn(produces);

  }

  private void setSerializer(String contentType) {
    given(serializerManager.getSerializer(contentType))
        .willReturn(serializer);
  }

  @Test
  public void testWhenAcceptIsEmptyThenShouldReturnDefaultContentType() {
    // Given
    setEnv(null, Sets.newHashSet(), Collections.emptyList(), false);
    setSerializer("application/json");


    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);

    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.toBlocking().first();

    // Then
    verify(errorHandler, never()).handleError(any(), anyBoolean(), any());
    verify(routeHandler).process(request, response);
    verify(serializer).serialize(eq(resultBody), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  }

  @Test
  public void testWhenAcceptIsJsonAndSupportedIsJsonAndProducesIsEmptyThenShouldReturnJson() {

  }
}