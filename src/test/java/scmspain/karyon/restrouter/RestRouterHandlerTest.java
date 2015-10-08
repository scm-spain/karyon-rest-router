package scmspain.karyon.restrouter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.reactivex.netty.protocol.http.server.HttpRequestHeaders;
import io.reactivex.netty.protocol.http.server.HttpResponseHeaders;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import rx.Observable;
import rx.observers.TestSubscriber;
import scmspain.karyon.restrouter.exception.CannotSerializeException;
import scmspain.karyon.restrouter.exception.InvalidAcceptHeaderException;
import scmspain.karyon.restrouter.handlers.ErrorHandler;
import scmspain.karyon.restrouter.serializer.SerializeManager;
import scmspain.karyon.restrouter.serializer.Serializer;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;
import scmspain.karyon.restrouter.transport.http.Route;
import scmspain.karyon.restrouter.transport.http.RouteHandler;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by pablo.diaz on 6/10/15.
 */
public class RestRouterHandlerTest {
  TestSubscriber<Void> subscriber = new TestSubscriber<>();

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

    given(response.getAllocator())
        .willReturn(new PooledByteBufAllocator());

    given(restUriRouter.findBestMatch(request, response))
        .willReturn(Optional.of(route));

    given(route.getHandler())
        .willReturn(routeHandler);

    given(serializerManager.getDefaultContentType())
        .willReturn("application/json");

    given(routeHandler.process(request, response))
        .willReturn(Observable.just(resultBody));

    given(serializerManager.getSupportedMediaTypes())
        .willReturn(ImmutableSet.of());

  }

  private void setAccept(String accept) {
    given(request.getHeaders().get(HttpHeaders.ACCEPT))
        .willReturn(accept);
  }

  private void setSupportedContents(String... supported) {
    for(String contentType: supported) {
      given(serializerManager.getSerializer(contentType))
          .willReturn(serializer);
    }
    given(serializerManager.getSupportedMediaTypes())
        .willReturn(Sets.newHashSet(supported));
  }

  private void setProduces(String... produces) {
    LinkedHashSet<String> producesSet = new LinkedHashSet<>();
    Stream.of(produces).forEach(producesSet::add);

    given(route.getProduces())
        .willReturn(producesSet);
  }

  private void setCustomRoute(boolean isCustom) {
    given(route.isCustom())
        .willReturn(isCustom);
  }


  // 3
  @Test
  public void testWhenAcceptIsEmptyThenShouldReturnDefaultContentType() {
    // Given
    setSupportedContents("application/json");
    setCustomRoute(false);

    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler, never()).handleError(any(), any());
    verify(routeHandler).process(request, response);
    verify(serializer).serialize(eq(resultBody), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  }

  // 5
  @Test
  public void testWhenAcceptIsJsonAndSupportedIsJsonAndProducesIsEmptyThenShouldReturnJson() {
    // Given
    setAccept("application/json");
    setSupportedContents("application/json");
    setCustomRoute(false);

    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler, never()).handleError(any(), any());
    verify(routeHandler).process(request, response);
    verify(serializer).serialize(eq(resultBody), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  }

  // 7
  @Test
  public void testWhenAcceptIsEmptySupportedIsJsonAndProducesIsJsonThenShouldReturnJson() {
    // Given
    setSupportedContents("application/json");
    setProduces("application/json");
    setCustomRoute(false);

    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler, never()).handleError(any(), any());
    verify(routeHandler).process(request, response);
    verify(serializer).serialize(eq(resultBody), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  }

  // 8
  @Test
  public void testWhenAcceptIsNotValidAndSupportedIsJsonThenShouldReturnDefaultContentType() {
    // Given
    setAccept("invalid_accept");
    setSupportedContents("application/json");
    setCustomRoute(false);

    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler).handleError(isA(InvalidAcceptHeaderException.class), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

  }

  // 9
  @Test
  public void testWhenAcceptIsJsonAndSupportedIsJsonAndProducesIsJsonThenShouldReturnJson() {
    // Given
    setAccept("application/json");
    setSupportedContents("application/json");
    setProduces("application/json");
    setCustomRoute(false);

    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler, never()).handleError(any(), any());
    verify(routeHandler).process(request, response);
    verify(serializer).serialize(eq(resultBody), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

  }

  // 11
  @Test
  public void testWhenAcceptIsXmlAndSupportedIsJsonThenShouldReturnCannotSerializedInJson() {
    // Given
    setAccept("text/xml");
    setSupportedContents("application/json");
    setCustomRoute(false);

    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler).handleError(isA(CannotSerializeException.class), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

  }

  // 14
  @Test
  public void testWhenAcceptIsEmptyAndSupportedAreJsonAndXmlAndProducesXmlThenReturnXml() {
    // Given
    setAccept(null);
    setSupportedContents("application/json", "text/xml");
    setCustomRoute(false);
    setProduces("text/xml");


    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler, never()).handleError(any(), any());
    verify(routeHandler).process(request, response);
    verify(serializer).serialize(eq(resultBody), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "text/xml");
  }

  // 15
  @Test
  public void testWhenAcceptIsEmptyAndSupportedAreJsonAndXmlAndProducesAreXmlAndJsonThenShouldReturnDefault() {
    // Given
    setAccept(null);
    setSupportedContents("application/json", "text/xml");
    setCustomRoute(false);
    setProduces("text/xml","application/json");


    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler, never()).handleError(any(), any());
    verify(routeHandler).process(request, response);
    verify(serializer).serialize(eq(resultBody), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  }

  // 16
  @Test
  public void testWhenAcceptIsEmptyAndSupportedAreJsonAndXmlAndTestAndProducesAreXmlAndTextThenShouldReturnCannotSerialize() {
    // Given
    setAccept(null);
    setSupportedContents("application/json", "text/xml", "text/plain");
    setCustomRoute(false);
    setProduces("text/xml", "text/plain");


    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    subscriber.assertReceivedOnNext(Collections.emptyList());
    verify(errorHandler).handleError(isA(CannotSerializeException.class), any());
    verify(response.getHeaders()).setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
  }

  // 17
  @Test
  public void testWhenAcceptIsEmptyAndSupportedAreJsonAndXmlAndTestAndProducesCustomThenShouldReturnCannotSerialize() {
    // Given
    setAccept(null);
    setSupportedContents("application/json");
    setCustomRoute(true);
    setProduces("text/xml");


    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    // Then
    //  If it's custom the elements in the observable will remain in there as it wouldn't be touch
    //  TODO: should we throw an exception in those cases?
    assertThat(subscriber.getOnNextEvents().size(), is(1));

    verify(errorHandler, never()).handleError(any(), any());
    verify(routeHandler).process(request, response);
    verify(serializer, never()).serialize(any(), any());
    verify(response.getHeaders(), never()).setHeader(any(), any());
  }

  @Test
  public void testWhenAcceptIsEmptyAndSupportedAreJsonAndXmlAndTestAndProducesCustomAndHandlerReturnsException() {
    // Given
    setAccept(null);
    setSupportedContents("application/json");
    setCustomRoute(true);
    setProduces("text/xml");

    given(routeHandler.process(request, response))
        .willReturn(Observable.error(new RuntimeException("test")));


    // When
    RestRouterHandler restRouterHandler = new RestRouterHandler(restUriRouter, errorHandler, serializerManager);
    Observable<Void> responseBody = restRouterHandler.handle(request, response);

    responseBody.subscribe(subscriber);

    List<Throwable> throwableList = subscriber.getOnErrorEvents();

    // Then
    verify(routeHandler).process(request, response);
    verify(serializer, never()).serialize(any(), any());
    verify(response.getHeaders(), never()).setHeader(any(), any());

    assertThat(throwableList, hasItem(Matchers.isA(RuntimeException.class)));
  }

  // TODO: More tests, check Box excel file

}