package com.example.sal.api;

import com.example.sal.domain.CompressionType;
import com.example.sal.domain.SalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateSalRequest {
  @NotBlank private String salName;
  private String salDescription;
  @NotNull private SalType salType;
  private String salMetadataJson;
  private boolean compressed = false;
  private CompressionType compressionType = CompressionType.NONE;
  @NotBlank private String ownerId;
  @NotBlank private String lstModUser;

  public String getSalName() { return salName; }
  public void setSalName(String salName) { this.salName = salName; }
  public String getSalDescription() { return salDescription; }
  public void setSalDescription(String salDescription) { this.salDescription = salDescription; }
  public SalType getSalType() { return salType; }
  public void setSalType(SalType salType) { this.salType = salType; }
  public String getSalMetadataJson() { return salMetadataJson; }
  public void setSalMetadataJson(String salMetadataJson) { this.salMetadataJson = salMetadataJson; }
  public boolean isCompressed() { return compressed; }
  public void setCompressed(boolean compressed) { this.compressed = compressed; }
  public CompressionType getCompressionType() { return compressionType; }
  public void setCompressionType(CompressionType compressionType) { this.compressionType = compressionType; }
  public String getOwnerId() { return ownerId; }
  public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
  public String getLstModUser() { return lstModUser; }
  public void setLstModUser(String lstModUser) { this.lstModUser = lstModUser; }
}
