package scmspain.karyon.restrouter.transport.http;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by pablo.diaz on 28/9/15.
 */
public class RouteInterceptorSupportTest {
  @Mock
  HttpServerRequest<ByteBuf> request;
  @Mock
  HttpServerResponse<ByteBuf> response;

  @Before
  public void setUp() {
    initMocks(this);

  }

  private RouteOutInterceptor newMockInterceptor() {

    RouteOutInterceptor interceptor = mock(RouteOutInterceptor.class);
    when(interceptor.intercept(any(), any(), any())).thenReturn(Observable.empty());

    return interceptor;
  }

  @Test
  public void whenInterceptANonVoidObservableWithoutAdditionalInterceptorsItShouldSendAnError() {
    // Given
    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    RouteInterceptorSupport routeInterceptorSupport = new RouteInterceptorSupport();
    Observable<Object> responseBodyObs = Observable.just(1);

    // When
    Observable<Void> result = routeInterceptorSupport.execute(responseBodyObs, request, response);
    result.subscribe(subscriber);

    // Then
    assertThat(subscriber.getOnErrorEvents().size(), is(1));

  }

  @Test
  public void whenInterceptAVoidObservableWithoutAdditionalInterceptorsItShouldSendAnError() {
    // Given
    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    RouteInterceptorSupport routeInterceptorSupport = new RouteInterceptorSupport();
    Observable<Object> responseBodyObs = Observable.just(null);

    // When
    Observable<Void> result = routeInterceptorSupport.execute(responseBodyObs, request, response);
    result.subscribe(subscriber);

    // Then
    subscriber.assertNoErrors();
    subscriber.assertReceivedOnNext(Collections.emptyList());

  }

  @Test
  public void interceptorShouldExecute() {
    // Given
    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    RouteInterceptorSupport routeInterceptorSupport = new RouteInterceptorSupport();
    Observable<Object> responseBodyObs = Observable.just(null);
    RouteOutInterceptor mockInterceptor = newMockInterceptor();

    routeInterceptorSupport.addOutInterceptor(mockInterceptor);

    // When
    Observable<Void> result = routeInterceptorSupport.execute(responseBodyObs, request, response);
    result.subscribe(subscriber);

    // Then
    subscriber.assertNoErrors();
    subscriber.assertReceivedOnNext(Collections.emptyList());

    verify(mockInterceptor, times(1)).intercept(any(), same(request), same(response));
  }

  @Test
  public void interceptorShouldExecuteInOrder() {
    // Given
    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    RouteInterceptorSupport routeInterceptorSupport = new RouteInterceptorSupport();
    Observable<Object> responseBodyObs = Observable.just(null);
    RouteOutInterceptor mockInterceptor1 = newMockInterceptor();
    RouteOutInterceptor mockInterceptor2 = newMockInterceptor();
    RouteOutInterceptor mockInterceptor3 = newMockInterceptor();
    RouteOutInterceptor mockInterceptor4 = newMockInterceptor();


    routeInterceptorSupport.addOutInterceptor(mockInterceptor1);
    routeInterceptorSupport.addOutInterceptor(mockInterceptor2);
    routeInterceptorSupport.addOutInterceptor(mockInterceptor3);
    routeInterceptorSupport.addOutInterceptor(mockInterceptor4);

    // When
    Observable<Void> result = routeInterceptorSupport.execute(responseBodyObs, request, response);
    result.subscribe(subscriber);

    // Then
    subscriber.assertNoErrors();
    subscriber.assertReceivedOnNext(Collections.emptyList());

    InOrder inOrder = inOrder(mockInterceptor1, mockInterceptor2, mockInterceptor3, mockInterceptor4);

    inOrder.verify(mockInterceptor1, times(1)).intercept(any(), same(request), same(response));
    inOrder.verify(mockInterceptor2, times(1)).intercept(any(), same(request), same(response));
    inOrder.verify(mockInterceptor3, times(1)).intercept(any(), same(request), same(response));
    inOrder.verify(mockInterceptor4, times(1)).intercept(any(), same(request), same(response));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void whenInterceptorThrowsAnExceptionItShouldEncapsulateInObservable() {
    // Given
    TestSubscriber<Void> subscriber = new TestSubscriber<>();
    RouteInterceptorSupport routeInterceptorSupport = new RouteInterceptorSupport();
    Observable<Object> responseBodyObs = Observable.just(null);
    RouteOutInterceptor mockInterceptor = mock(RouteOutInterceptor.class);

    given(mockInterceptor.intercept(any(), any(), any())).willThrow(new RuntimeException("Error during interception"));

    routeInterceptorSupport.addOutInterceptor(mockInterceptor);

    // When
    Observable<Void> result = routeInterceptorSupport.execute(responseBodyObs, request, response);
    result.subscribe(subscriber);

    // Then
    assertThat(subscriber.getOnErrorEvents(), is(not(empty())));

    verify(mockInterceptor, times(1)).intercept(any(), same(request), same(response));
  }

}