package scmspain.karyon.restrouter;

import com.google.inject.Injector;
import com.netflix.hystrix.contrib.rxnetty.metricsstream.HystrixMetricsStreamHandler;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class RouteHandlerProviderTest {

  private Injector mockedInjector;
  private RouteHandlerProvider provider;
  private RequestHandler<ByteBuf, ByteBuf> usingRouter;

  @Before
  public void setUp() {
    mockedInjector = Mockito.mock(Injector.class);
  }

  @Test
  public void itShouldReturnRestRouterHandlerWhenMetricsAreDisabledInConfiguration()
    throws Exception {

    givenMetricsDisabledByConfiguration();
    whenRouteHandlerProviderIsInitialized();
    andCallIt();
    thenUseRestRouterHandler();
  }

  @Test
  public void itShouldReturnHystrixMetricsStreamHandlerWhenEnabledInConfiguration()
    throws Exception {

    givenMetricsEnabledByConfiguration();
    whenRouteHandlerProviderIsInitialized();
    andCallIt();
    thenHystrixMetricsStreamHandlerIsTheRouter();
    andUseRestRouterHandlerAsDelegatedRouterHandler();
  }

  private void givenMetricsDisabledByConfiguration() throws Exception {
    stubMetricsStateByConfiguration(false);
  }

  private void givenMetricsEnabledByConfiguration() throws Exception {
    stubMetricsStateByConfiguration(true);
  }

  private void whenRouteHandlerProviderIsInitialized() {
    provider = new RouteHandlerProvider(mockedInjector);
  }

  private void andCallIt() {
    usingRouter = provider.get();
  }

  private void thenHystrixMetricsStreamHandlerIsTheRouter() {
    assertThat(usingRouter, is(instanceOf(HystrixMetricsStreamHandler.class)));
  }

  private void thenUseRestRouterHandler() {
    verify(mockedInjector, atLeastOnce()).getInstance(RestRouterHandler.class);
  }

  private void andUseRestRouterHandlerAsDelegatedRouterHandler() {
    thenUseRestRouterHandler();
  }

  private void stubMetricsStateByConfiguration(boolean state) throws Exception {
    RouteHandlerProvider mockedProvider = Mockito.mock(RouteHandlerProvider.class);
    Mockito.when(mockedProvider.isMetricsStreamEnabled()).thenReturn(state);

    PowerMockito
      .whenNew(RouteHandlerProvider.class)
      .withArguments(mockedInjector)
      .thenReturn(mockedProvider);
  }
}
