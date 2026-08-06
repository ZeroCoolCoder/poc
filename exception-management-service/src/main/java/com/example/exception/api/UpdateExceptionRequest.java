package com.example.exception.api;

import com.example.exception.domain.ExceptionType;
import com.example.exception.domain.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateExceptionRequest {

  private ExceptionType exceptionType;

  @Size(max = 1000)
  private String technicalDescription;

  @Size(max = 1000)
  private String businessDescription;

  private Integer httpStatusCode;

  private Severity severity;

  @NotBlank
  @Size(max = 10)
  private String lstModUser;

  public ExceptionType getExceptionType() { return exceptionType; }
  public void setExceptionType(ExceptionType exceptionType) { this.exceptionType = exceptionType; }

  public String getTechnicalDescription() { return technicalDescription; }
  public void setTechnicalDescription(String technicalDescription) { this.technicalDescription = technicalDescription; }

  public String getBusinessDescription() { return businessDescription; }
  public void setBusinessDescription(String businessDescription) { this.businessDescription = businessDescription; }

  public Integer getHttpStatusCode() { return httpStatusCode; }
  public void setHttpStatusCode(Integer httpStatusCode) { this.httpStatusCode = httpStatusCode; }

  public Severity getSeverity() { return severity; }
  public void setSeverity(Severity severity) { this.severity = severity; }

  public String getLstModUser() { return lstModUser; }
  public void setLstModUser(String lstModUser) { this.lstModUser = lstModUser; }
}
