package scmspain.karyon.restrouter;


import com.netflix.governator.guice.BootstrapModule;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import netflix.karyon.Karyon;
import netflix.karyon.KaryonServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.Observable;
import scmspain.karyon.restrouter.core.AppServer;

public class KaryonRestRouterTest {

  private static KaryonServer server;

  @BeforeClass
  public static void setUpBefore() throws Exception {
    server = Karyon.forApplication(AppServer.class, (BootstrapModule[]) null);
    server.start();
  }

  @AfterClass
  public static void cleanUpAfter() throws Exception {
    server.shutdown();
  }

  @Test
  public void testEndpointGET() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createGet("/example/1"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("Example endpoint controller with GET!", body);


  }

  @Test
  public void testEndpointGETWithQueryParamFilter() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createGet("/example_query_params/1?filter=barcelona"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("{\"id\":\"1\",\"filter\":\"barcelona\"}", body);


  }

  @Test
  public void testEndpointGETWithQueryParamFilterAndDefaultValue() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createGet("/example_query_param_default/1"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("{\"id\":\"1\",\"filter\":\"forlayo\"}", body);


  }

  @Test
  public void testEndpointGETWithQueryParamFilterAndOverrideDefaultValue() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createGet("/example_query_param_default/1?filter=minglanillas"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("{\"id\":\"1\",\"filter\":\"minglanillas\"}", body);


  }

  @Test
  public void testEndpointGETWithQueryParamFilterAndOverrideDefaultValueWithRequired() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createGet("/example_query_param_default_required/1?filter=minglanillas"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("{\"id\":\"1\",\"filter\":\"minglanillas\"}", body);


  }

  @Test
  public void testEndpointGETWithoutQueryParamFilterWithRequired() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createGet("/example_without_query_param_and_required/1"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), response.getStatus().code());
              return response.getContent()
                  .defaultIfEmpty(new EmptyByteBuf(ByteBufAllocator.DEFAULT))
                  .map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);

    Assert.assertEquals("", body);

  }


  @Test
  public void testEndpointPOST() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createPost("/example_with_post_method"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("Example endpoint controller with POST!", body);

  }

  @Test
  public void testEndpointPUT() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createPut("/example_with_put_method"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("Example endpoint controller with PUT!", body);

  }

  @Test
  public void testEndpointDELETE() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createDelete("/example_with_delete_method"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("Example endpoint controller with DELETE!", body);
  }

  @Test
  public void testEndpointPrecedence() throws Exception {
      String body =
              RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
                      .submit(HttpClientRequest.createGet("/example_path/hardcoded_param"))
                      .flatMap(response -> {
                        Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
                        return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
                      })
                      .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
      Assert.assertEquals("I'm the hardcoded one", body);
  }

  @Test
  public void testPathNotFound() throws Exception {
    HttpResponseStatus status =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createGet("/unexistant_path"))
            .map(response -> response.getStatus())
            .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals(HttpResponseStatus.NOT_FOUND, status);
  }

  @Test
  public void testEndpointDefaultQueryParamSystem() throws Exception {

    String body =
            RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
                    .submit(HttpClientRequest.createGet("/example_path_default_system"))
                    .flatMap(response -> {
                      Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
                      return response.getContent().<String>map(content -> content.toString(Charset.defaultCharset()));
                    })
                    .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    Assert.assertEquals("I'm the default queryparam", body);

  }


  @Test
  public void testEndpointWithQueryParamsReturnIntegerObs() throws Exception {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    String body =
        RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
            .submit(HttpClientRequest.createGet("/example_json_interceptor/1?filter=barcelona"))
            .flatMap(response -> {
              Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
              return response.getContent().<String>map(content -> {
                if (content == null) {
                  return "";
                } else {
                  return content.toString(Charset.defaultCharset());
                }
              });
            })
            .finallyDo(() -> finishLatch.countDown())
            .toBlocking().first();

    Assert.assertEquals("{\"value\":\"1\"}", body);
  }

  @Test
  public void testHystrixStreamEndpointReturnsOk() throws Exception {

    // This request is wrapper in a Hystrix command because, since /hystrix.stream does not send
    // response's headers until it send the first byte, and to do so need some metrics to exists,
    // it needs any Hystrix command to be executed to start the stream.
    new HystrixObservableCommand<HttpClientResponse<ByteBuf>>(HystrixCommandGroupKey.Factory.asKey("request")) {
      @Override
      protected Observable<HttpClientResponse<ByteBuf>> construct() {
        return RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
          .submit(HttpClientRequest.createGet("/example/1"));
      }
    }.construct().map(this::callToHystrixStreamEndpoint);
  }

  private String callToHystrixStreamEndpoint(HttpClientResponse<ByteBuf> any) throws RuntimeException {
    try {
      return RxNetty.createHttpClient("localhost", AppServer.KaryonRestRouterModuleImpl.DEFAULT_PORT)
        .submit(HttpClientRequest.createGet("/hystrix.stream"))
        .flatMap(response -> {
          Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatus().code());
          return response.getContent().map(content -> content.toString(Charset.defaultCharset()));
        })
        .toBlocking().toFuture().get(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
