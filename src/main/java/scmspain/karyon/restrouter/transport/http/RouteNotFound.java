package scmspain.karyon.restrouter.transport.http;

import scmspain.karyon.restrouter.RouteNotFoundHandler;

import java.util.Collections;


public class RouteNotFound<I,O> extends Route<I,O> {
  public RouteNotFound() {
    super("not_found", (request, context) -> true, Collections.emptyList(), false, new RouteNotFoundHandler<>());
  }

}
