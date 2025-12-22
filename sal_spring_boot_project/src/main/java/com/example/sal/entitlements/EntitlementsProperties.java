package com.example.sal.entitlements;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sal.entitlements")
public class EntitlementsProperties {
  private String baseUrl = "http://localhost:8089";
  private String checkPath = "/api/v1/check";
  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public String getCheckPath() { return checkPath; }
  public void setCheckPath(String checkPath) { this.checkPath = checkPath; }
}
