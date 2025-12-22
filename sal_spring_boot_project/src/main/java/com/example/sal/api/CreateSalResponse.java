package com.example.sal.api;

public class CreateSalResponse {
  private String salUuid;
  private long version;

  public CreateSalResponse() {}
  public CreateSalResponse(String salUuid, long version) { this.salUuid = salUuid; this.version = version; }
  public String getSalUuid() { return salUuid; }
  public void setSalUuid(String salUuid) { this.salUuid = salUuid; }
  public long getVersion() { return version; }
  public void setVersion(long version) { this.version = version; }
}
