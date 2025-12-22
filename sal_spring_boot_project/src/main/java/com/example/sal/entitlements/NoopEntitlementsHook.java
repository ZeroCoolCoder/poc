package com.example.sal.entitlements;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Profile("!entitlements")
public class NoopEntitlementsHook implements EntitlementsHook {
  @Override
  public void assertAllowed(String subject, String action, String salUuid, Long version, Map<String, Object> attributes) {
    // allow
  }
}
