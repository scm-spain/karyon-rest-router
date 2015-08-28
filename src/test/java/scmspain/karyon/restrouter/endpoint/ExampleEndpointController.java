package scmspain.karyon.restrouter.endpoint;

import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import javax.ws.rs.HttpMethod;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.annotation.PathParam;
import scmspain.karyon.restrouter.annotation.QueryParam;

@Singleton
@Endpoint
public class ExampleEndpointController {

  public ExampleEndpointController() {

  }


  @Path(value = "/example/{id}", method = HttpMethod.GET)
   public Observable<Void> getEndpointResource(HttpServerResponse<ByteBuf> response, @PathParam("id") String id) {

    //Example to get parameters from @Path

    return response.writeStringAndFlush("Example endpoint controller with GET!")
        .concatWith(response.close());

  }

  @Path(value = "/example_query_params/{id}", method = HttpMethod.GET)
  public Observable<Void> getEndpointWithQueryParamsResource(HttpServerResponse<ByteBuf> response, @PathParam("id") String id, @QueryParam("filter") String filter) {

    response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
    String jsonResponse = String.format("{\"id\":\"%s\",\"filter\":\"%s\"}",id,filter);
    return response.writeStringAndFlush(jsonResponse);

  }

  @Path(value = "/example_query_param_default/{id}", method = HttpMethod.GET)
  public Observable<Void> getEndpointWithQueryParamAndDefaultValueResource(HttpServerResponse<ByteBuf> response, @PathParam("id") String id, @QueryParam(value = "filter",defaultValue = "forlayo") String filter) {

    response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
    String jsonResponse = String.format("{\"id\":\"%s\",\"filter\":\"%s\"}",id,filter);
    return response.writeStringAndFlush(jsonResponse);

  }

  @Path(value = "/example_query_param_default_required/{id}", method = HttpMethod.GET)
  public Observable<Void> getEndpointWithQueryParamAndDefaultValueRequiredResource(HttpServerResponse<ByteBuf> response, @PathParam("id") String id, @QueryParam(value = "filter",defaultValue = "forlayo",required = true) String filter) {

    response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
    String jsonResponse = String.format("{\"id\":\"%s\",\"filter\":\"%s\"}",id,filter);
    return response.writeStringAndFlush(jsonResponse);

  }

  @Path(value = "/example_without_query_param_and_required/{id}", method = HttpMethod.GET)
  public Observable<Void> getEndpointWithoutQueryParamAndRequiredResource(HttpServerResponse<ByteBuf> response, @PathParam("id") String id, @QueryParam(value = "filter",required = true) String filter) {

    response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
    String jsonResponse = String.format("{\"id\":\"%s\",\"filter\":\"%s\"}",id,filter);
    return response.writeStringAndFlush(jsonResponse);

  }


}

