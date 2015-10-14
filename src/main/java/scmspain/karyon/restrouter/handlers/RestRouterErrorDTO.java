package scmspain.karyon.restrouter.handlers;

import java.time.Instant;

public final class RestRouterErrorDTO {

  private String description;
  private String timestamp;



  RestRouterErrorDTO(String description) {
    this.description = description;
    this.timestamp = Instant.now().toString();
  }

  public String getDescription() {
    return description;
  }

  public String getTimestamp() {
    return timestamp;
  }
}
