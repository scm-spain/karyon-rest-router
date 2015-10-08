package scmspain.karyon.restrouter.transport.http;

import scmspain.karyon.restrouter.RouteNotFoundHandler;

import java.util.Collections;

/**
 * Created by pablo.diaz on 8/10/15.
 */
public class RouteNotFound<I,O> extends Route<I,O> {
  public RouteNotFound() {
    super((request, context) -> true, Collections.emptyList(), new RouteNotFoundHandler<>());
  }

  @Override
  public boolean isCustom() {
    return false;
  }
}
