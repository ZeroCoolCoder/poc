package com.example.exception.domain;

import java.time.Instant;

public class ExceptionRecord {

  private String exceptionCode;
  private ExceptionType exceptionType;
  private String technicalDescription;
  private String businessDescription;
  private int httpStatusCode;
  private Severity severity;
  private boolean active;
  private String lstModChgCd;
  private String lstModUser;
  private Instant lstModTs;

  public String getExceptionCode() { return exceptionCode; }
  public void setExceptionCode(String exceptionCode) { this.exceptionCode = exceptionCode; }

  public ExceptionType getExceptionType() { return exceptionType; }
  public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }

  public String getTechnicalDescription() { return technicalDescription; }
  public void setTechnicalDescription(String technicalDescription) { this.technicalDescription = technicalDescription; }

  public String getBusinessDescription() { return businessDescription; }
  public void setBusinessDescription(String businessDescription) { this.businessDescription = businessDescription; }

  public int getHttpStatusCode() { return httpStatusCode; }
  public void setHttpStatusCode(int httpStatusCode) { this.httpStatusCode = httpStatusCode; }

  public Severity getSeverity() { return severity; }
  public void setSeverity(Severity severity) { this.severity = severity; }

  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }

  public String getLstModChgCd() { return lstModChgCd; }
  public void setLstModChgCd(String lstModChgCd) { this.lstModChgCd = lstModChgCd; }

  public String getLstModUser() { return lstModUser; }
  public void setLstModUser(String lstModUser) { this.lstModUser = lstModUser; }

  public Instant getLstModTs() { return lstModTs; }
  public void setLstModTs(Instant lstModTs) { this.lstModTs = lstModTs; }
}
