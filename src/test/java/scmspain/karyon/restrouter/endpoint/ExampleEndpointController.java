package scmspain.karyon.restrouter.endpoint;

import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import javax.ws.rs.HttpMethod;
import rx.Observable;
import scmspain.karyon.restrouter.annotation.CustomSerialization;
import scmspain.karyon.restrouter.annotation.Endpoint;
import scmspain.karyon.restrouter.annotation.Path;
import scmspain.karyon.restrouter.annotation.PathParam;
import scmspain.karyon.restrouter.annotation.QueryParam;
import scmspain.karyon.restrouter.endpoint.dto.ExampleDTO;

@Singleton
@Endpoint()
public class ExampleEndpointController {

  public ExampleEndpointController() {

  }

  @Path(value = "/example_json_interceptor/{id}", method = HttpMethod.GET, customSerialization = CustomSerialization.FALSE)
  public Observable<ExampleDTO> getEndpointWithQueryParamsReturnDTOToPrintJson(HttpServerResponse<ByteBuf> response, @PathParam("id") String id, @QueryParam("filter") String filter) {
    response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
    return Observable.just(new ExampleDTO(id));
  }


  @Path(value = "/example/{id}", method = HttpMethod.GET)
   public Observable<Void> getEndpointResource(HttpServerResponse<ByteBuf> response, @PathParam("id") String id) {

    //Example to get parameters from @Path

    return response.writeStringAndFlush("Example endpoint controller with GET!");

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

  @Path(value = "/example_with_post_method", method = HttpMethod.POST)
  public Observable<Void> postEndpoint(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {

    return response.writeStringAndFlush("Example endpoint controller with POST!");

  }

  @Path(value = "/example_with_put_method", method = HttpMethod.PUT)
  public Observable<Void> putEndpoint(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {

    return response.writeStringAndFlush("Example endpoint controller with PUT!");

  }

  @Path(value = "/example_with_delete_method", method = HttpMethod.DELETE)
  public Observable<Void> deleteEndpoint(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {

    return response.writeStringAndFlush("Example endpoint controller with DELETE!");

  }

  //Test path precedence
  @Path(value = "/example_path/hardcoded_param", method = HttpMethod.GET)
  public Observable<Void> hardcodedPathEndpoint(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
    return response.writeStringAndFlush("I'm the hardcoded one");
  }

  @Path(value = "/example_path/{parameterized_param}", method = HttpMethod.GET)
  public Observable<Void> parameterizedPathEndpoint(HttpServerRequest<ByteBuf> request, @PathParam("parameterized_param") String p, HttpServerResponse<ByteBuf> response) {
    return response.writeStringAndFlush("I'm the parameterized one");
  }

  @Path(value = "/example_path_default_system", method = HttpMethod.GET)
  public Observable<Void> defaultValueEndpoint(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response, @QueryParam(value = "Ni") String filter) {
    return response.writeStringAndFlush("I'm the default queryparam"+filter);
  }

}

