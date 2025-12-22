package com.example.sal.entitlements;

import java.util.Map;

public interface EntitlementsHook {
  void assertAllowed(String subject, String action, String salUuid, Long version, Map<String, Object> attributes);
}
