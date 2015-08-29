package scmspain.karyon.restrouter;


import com.netflix.governator.guice.BootstrapModule;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import netflix.karyon.Karyon;
import netflix.karyon.KaryonServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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
}
