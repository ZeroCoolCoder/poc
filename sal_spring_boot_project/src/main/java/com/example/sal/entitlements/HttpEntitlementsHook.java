package com.example.sal.entitlements;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
@Profile("entitlements")
public class HttpEntitlementsHook implements EntitlementsHook {
  private final RestClient restClient;
  private final EntitlementsProperties props;

  public HttpEntitlementsHook(RestClient.Builder builder, EntitlementsProperties props) {
    this.restClient = builder.baseUrl(props.getBaseUrl()).build();
    this.props = props;
  }

  public record Decision(boolean allowed, String reason) {}
  public record CheckRequest(String subject, String action, String salUuid, Long version, Map<String, Object> attributes) {}

  @Override
  public void assertAllowed(String subject, String action, String salUuid, Long version, Map<String, Object> attributes) {
    Decision d = restClient.post()
        .uri(props.getCheckPath())
        .contentType(MediaType.APPLICATION_JSON)
        .body(new CheckRequest(subject, action, salUuid, version, attributes))
        .retrieve()
        .body(Decision.class);

    if (d == null || !d.allowed()) {
      throw new org.springframework.security.access.AccessDeniedException(d != null ? d.reason() : "Denied");
    }
  }
}
