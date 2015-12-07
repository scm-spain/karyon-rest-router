package scmspain.karyon.restrouter;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import io.netty.buffer.ByteBuf;
import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.annotation.Produces;
import scmspain.karyon.restrouter.core.MethodParameterResolver;
import scmspain.karyon.restrouter.core.ResourceLoader;
import scmspain.karyon.restrouter.core.URIParameterParser;
import scmspain.karyon.restrouter.dummy.DummyController;
import scmspain.karyon.restrouter.endpoint.ExampleEndpointController;
import scmspain.karyon.restrouter.transport.http.RestUriRouter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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

  private synchronized void doWithPackageConfig(Runnable runnable) {
    doWithPackageConfig("", runnable);
  }

  /**
   * <p>
   *  This high order function runs some code after mocking the static Archaius configuration to
   *  return the value expected as package configuration. When It's finish it tries to leave the
   *  Archaius state the same as previously.
   * </p>
   * <p>
   *  This function needs to be synchronized to be "thread safe", but it's really only threadsafe
   *  in terms of this test, as if someone plays with Archaius configuration on another thread the
   *  behavior is warranted to be very funny.
   * </p>
   * <p>
   *   Unfortunately this is a clear example of what happens when you use static values.
   * </p>
   *
   * @param packagePropertyValue the value you want to be returned as package scan
   * {@link RestRouterScanner#BASE_PACKAGE_PROPERTY}
   * @param runnable the piece of code to be executed with this scenario
   * @throws RuntimeException if it doesn't know how to mock Archaius
   */
  private synchronized void doWithPackageConfig(String packagePropertyValue, Runnable runnable) {
    AbstractConfiguration abstractConfiguration = ConfigurationManager.getConfigInstance();

    if(abstractConfiguration instanceof ConcurrentCompositeConfiguration) {
      ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration)abstractConfiguration;

      Map<String, AbstractConfiguration> oldConfigMap = config.getConfigurationNames()
          .stream()
          .collect(Collectors.toMap(Function.identity(), name -> (AbstractConfiguration)config.getConfiguration(name)));

      config.setProperty(RestRouterScanner.BASE_PACKAGE_PROPERTY, packagePropertyValue);

      runnable.run();

      oldConfigMap.entrySet().forEach(entry -> config.addConfiguration(entry.getValue(), entry.getKey()));

    } else {
      throw new RuntimeException("I don't know how to mock this, implement me (static hater)");
    }
  }

  @Test
  public void givenAEndPointWithoutMethodsItShouldInitializeOk() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(WithoutPathsAndMethods.class));

    // When
    doWithPackageConfig("something", () ->
      new RestRouterScanner(injector, parameterParser,
                  rmParameterInjector, resourceLoader, restUriRouter)
    );

    // Then
    verify(restUriRouter, never()).addUriRegex(any(), any(), any(), any(), eq(true), any());
  }

  @Test
  public void givenAEndPointWithoutPathMethodsItShouldInitializeOk() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(WithoutPaths.class));

    // When
    doWithPackageConfig(() ->
        new RestRouterScanner(injector, parameterParser,
        rmParameterInjector, resourceLoader, restUriRouter)
    );

    // Then
    verify(restUriRouter, never()).addUriRegex(any(), any(), any(), any(), eq(true), any());
  }

  @Test
  public void givenAEndPointWithPathMethodsItShouldInitializeWithARouteForEachMethod() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(With2Paths.class));

    // When
    doWithPackageConfig(() ->
      new RestRouterScanner(injector, parameterParser,
          rmParameterInjector, resourceLoader, restUriRouter)
    );

    // Then
    verify(restUriRouter, times(2)).addUriRegex(any(), any(), any(), any(), eq(true), any());
  }

  @Test
  public void givenAEndPointWithAPathMethodWithoutProducesItShouldSetRouteWithEmptyProduces() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(WithoutProduces.class));

    // When
    doWithPackageConfig(() ->
      new RestRouterScanner(injector, parameterParser,
          rmParameterInjector, resourceLoader, restUriRouter)
    );

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), any(), eq(Collections.emptyList()), eq(true), any());
  }

  @Test
  public void givenAEndPointWithAPathMethodWithProducesItShouldSetRouteWithEmptyProduces() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(With1ProducesTextPlain.class));

    // When
    doWithPackageConfig(() ->
      new RestRouterScanner(injector, parameterParser,
          rmParameterInjector, resourceLoader, restUriRouter)
    );

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), any(), producesCaptor.capture(), eq(true), any());

    Collection<String> col = producesCaptor.getValue();
    assertThat(col, contains("text/plain"));
  }

  @Test
  public void givenADefaultAnnotatedEndpointShouldBeCustom() {
    // Given
    given(resourceLoader.find(any(), eq(Endpoint.class)))
        .willReturn(Sets.newHashSet(DefaultEndpoint.class));

    // When
    doWithPackageConfig(() ->
      new RestRouterScanner(injector, parameterParser,
          rmParameterInjector, resourceLoader, restUriRouter)
    );

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), any(), any(), eq(true), any());
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
    doWithPackageConfig(() ->
      new RestRouterScanner(injector, parameterParser,
          rmParameterInjector, resourceLoader, restUriRouter)
    );

    // Then
    verify(restUriRouter).addUriRegex(any(), any(), any(), any(), eq(true), any());
  }

  @Test
  public void givenScanPropertyWithTwoPackagesWhenScanningThenHitsInTwoPackages() throws Exception {
    given(resourceLoader.find(eq("scmspain.karyon.restrouter.endpoint"), eq(Endpoint.class)))
      .willReturn(Sets.newHashSet(ExampleEndpointController.class));

    given(resourceLoader.find(eq("scmspain.karyon.restrouter.dummy"), eq(Endpoint.class)))
      .willReturn(Sets.newHashSet(DummyController.class));

    doWithPackageConfig("scmspain.karyon.restrouter.endpoint,scmspain.karyon.restrouter.dummy", () ->
      new RestRouterScanner(injector, parameterParser,
          rmParameterInjector, resourceLoader, restUriRouter)
    );

    verify(restUriRouter, atLeastOnce()).addUriRegex(
            Matchers.contains("ExampleEndpointController"), any(), any(), any(), eq(true), any());

    verify(restUriRouter, atLeastOnce()).addUriRegex(
            Matchers.contains("DummyController"), any(), any(), any(), eq(true), any());
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

  @Endpoint
  static class CustomMethodSerialization {
    @Path(value = "/path", method = "GET")
    public Observable<Void> path() {
      return null;
    }
  }


}