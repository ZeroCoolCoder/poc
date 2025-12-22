package com.example.sal.domain;

import java.time.Instant;

public class SalMetadataRecord {
  private String salUuid;
  private long version;
  private String salName;
  private String salDescription;
  private SalType salType;
  private String salMetadataJson;
  private Long sizeInBytes;
  private SalStatus status;
  private boolean latest;
  private boolean compressed;
  private CompressionType compressionType;
  private String ownerId;
  private String lstModChgCd;
  private String lstModUser;
  private Instant lstModTs;

  public String getSalUuid() { return salUuid; }
  public void setSalUuid(String salUuid) { this.salUuid = salUuid; }
  public long getVersion() { return version; }
  public void setVersion(long version) { this.version = version; }
  public String getSalName() { return salName; }
  public void setSalName(String salName) { this.salName = salName; }
  public String getSalDescription() { return salDescription; }
  public void setSalDescription(String salDescription) { this.salDescription = salDescription; }
  public SalType getSalType() { return salType; }
  public void setSalType(SalType salType) { this.salType = salType; }
  public String getSalMetadataJson() { return salMetadataJson; }
  public void setSalMetadataJson(String salMetadataJson) { this.salMetadataJson = salMetadataJson; }
  public Long getSizeInBytes() { return sizeInBytes; }
  public void setSizeInBytes(Long sizeInBytes) { this.sizeInBytes = sizeInBytes; }
  public SalStatus getStatus() { return status; }
  public void setStatus(SalStatus status) { this.status = status; }
  public boolean isLatest() { return latest; }
  public void setLatest(boolean latest) { this.latest = latest; }
  public boolean isCompressed() { return compressed; }
  public void setCompressed(boolean compressed) { this.compressed = compressed; }
  public CompressionType getCompressionType() { return compressionType; }
  public void setCompressionType(CompressionType compressionType) { this.compressionType = compressionType; }
  public String getOwnerId() { return ownerId; }
  public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
  public String getLstModChgCd() { return lstModChgCd; }
  public void setLstModChgCd(String lstModChgCd) { this.lstModChgCd = lstModChgCd; }
  public String getLstModUser() { return lstModUser; }
  public void setLstModUser(String lstModUser) { this.lstModUser = lstModUser; }
  public Instant getLstModTs() { return lstModTs; }
  public void setLstModTs(Instant lstModTs) { this.lstModTs = lstModTs; }
}
