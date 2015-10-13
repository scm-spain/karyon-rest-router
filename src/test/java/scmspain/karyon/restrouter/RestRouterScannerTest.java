package scmspain.karyon.restrouter;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.netty.buffer.ByteBuf;
import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.CustomSerialization;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.annotation.Produces;
import scmspain.karyon.restrouter.core.MethodParameterResolver;
import scmspain.karyon.restrouter.core.ResourceLoader;
import scmspain.karyon.restrouter.core.URIParameterParser;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class RestRouterScannerTest {
  @Mock
  private Injector injector;
  @Mock
  private URIParameterParser parameterParser;
  @Mock
  private MethodParameterResolver rmParameterInjector;
  @Mock
  private ResourceLoader resourceLoader;
  @Mock
  private RestUriRouter<ByteBuf, ByteBuf> restUriRouter;
  @Mock
  private AbstractConfiguration configuration;
  @Captor
  ArgumentCaptor<Collection<String>> producesCaptor;


  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void givenAEndPointWithoutMethodsItShouldInitializeOk() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(WithoutPathsAndMethods.class));

    // When
    new RestRouterScanner(injector, parameterParser,
                rmParameterInjector, resourceLoader, restUriRouter);

    // Then
    verify(restUriRouter, never()).addUriRegex(any(), any(), any(), eq(true), any());
  }

  @Test
  public void givenAEndPointWithoutPathMethodsItShouldInitializeOk() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(WithoutPaths.class));

    // When
    new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter);

    // Then
    verify(restUriRouter, never()).addUriRegex(any(), any(), any(), eq(true), any());
  }

  @Test
  public void givenAEndPointWithPathMethodsItShouldInitializeWithARouteForEachMethod() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(With2Paths.class));

    // When
    new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter);

    // Then
    verify(restUriRouter, times(2)).addUriRegex(any(), any(), any(), eq(true), any());
  }

  @Test
  public void givenAEndPointWithAPathMethodWithoutProducesItShouldSetRouteWithEmptyProduces() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(WithoutProduces.class));

    // When
    new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter);

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), eq(Collections.emptyList()), eq(true), any());
  }

  @Test
  public void givenAEndPointWithAPathMethodWithProducesItShouldSetRouteWithEmptyProduces() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(With1ProducesTextPlain.class));

    // When
    new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter);

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), producesCaptor.capture(), eq(true), any());

    Collection<String> col = producesCaptor.getValue();
    assertThat(col, contains("text/plain"));
  }

  @Test
  public void givenADefaultAnnotatedEndpointShouldBeCustom() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(DefaultEndpoint.class));

    // When
    new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter);

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), any(), eq(true), any());
  }

  @Test
  public void givenASerializableEndpointShouldBeNotBeCustom() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(EndpointWithSerialization.class));

    // When
    new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter);

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), any(), eq(false), any());
  }

  /*
  @Test
  public void givenAConfigurationWithoutSupportedContentWithAtLeastOneRouteNonCustomItShouldThrowAnException() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(EndpointWithSerialization.class));

    // When
    new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter);
  }
  */

  @Test
  public void givenAEndPointWithAMethodCustomSerializationItHasPreference() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(CustomMethodSerialization.class));

    // When
    new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter);

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), any(), eq(true), any());
  }


  @Endpoint
  static class WithoutPathsAndMethods {

  }

  @Endpoint
  static class DefaultEndpoint {
    @Path(value = "/path", method = "GET")
    public Observable<Void> path() {
      return null;
    }
  }

  @Endpoint(customSerialization = false)
  static class EndpointWithSerialization {
    @Path(value = "/path", method = "GET")
    public Observable<Void> path() {
      return null;
    }
  }

  @Endpoint
  static class WithoutPaths {
    void method() {

    }
    void method(int i) {

    }
  }

  @Endpoint
  static class With2Paths {
    void method() {

    }
    void method(int i) {

    }

    @Path(value = "/path", method = "GET")
    public Observable<Void> path() {
      return null;
    }

    @Path(value = "/path2", method = "GET")
    public Observable<Void> path2() {
      return null;
    }
  }

  @Endpoint
  static class WithoutProduces {
    @Path(value = "/path", method = "GET")
    public Observable<Void> method() {
      return null;
    }
  }

  @Endpoint
  static class With1ProducesTextPlain {
    @Path(value = "/path", method = "GET")
    @Produces("text/plain")
    public Observable<Void> method() {
      return null;
    }
  }

  @Endpoint(customSerialization = false)
  static class CustomMethodSerialization {
    @Path(value = "/path", method = "GET", customSerialization = CustomSerialization.TRUE)
    public Observable<Void> path() {
      return null;
    }
  }


}