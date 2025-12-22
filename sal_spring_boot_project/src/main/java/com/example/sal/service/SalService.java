package com.example.sal.service;

import com.example.sal.api.CreateSalRequest;
import com.example.sal.domain.SalMetadataRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalService {
  SalMetadataRecord create(CreateSalRequest req, String subject, InputStream bytes) throws IOException;
  SalMetadataRecord createNewVersion(String salUuid, CreateSalRequest req, String subject, InputStream bytes) throws IOException;
  SalMetadataRecord getLatest(String salUuid, String subject);
  SalMetadataRecord getVersion(String salUuid, long version, String subject);
  void downloadLatest(String salUuid, String subject, OutputStream out) throws IOException;
  void downloadVersion(String salUuid, long version, String subject, OutputStream out) throws IOException;
  List<SalMetadataRecord> search(Optional<String> name, Optional<String> ownerId,
                                Optional<LocalDate> from, Optional<LocalDate> to,
                                Optional<String> type, Optional<Boolean> latestOnly,
                                int limit, int offset, String subject);
}
