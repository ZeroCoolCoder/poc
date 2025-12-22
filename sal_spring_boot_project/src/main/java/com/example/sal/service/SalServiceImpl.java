package com.example.sal.service;

import com.example.sal.api.CreateSalRequest;
import com.example.sal.domain.*;
import com.example.sal.entitlements.EntitlementsHook;
import com.example.sal.repo.SalMetadataHistRepository;
import com.example.sal.repo.SalMetadataRepository;
import com.example.sal.storage.StorageHandlerFactory;
import com.example.sal.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SalServiceImpl implements SalService {

  private final SalMetadataRepository repo;
  private final SalMetadataHistRepository histRepo;
  private final StorageHandlerFactory handlers;
  private final EntitlementsHook entitlements;

  public SalServiceImpl(SalMetadataRepository repo,
                        SalMetadataHistRepository histRepo,
                        StorageHandlerFactory handlers,
                        EntitlementsHook entitlements) {
    this.repo = repo;
    this.histRepo = histRepo;
    this.handlers = handlers;
    this.entitlements = entitlements;
  }

  @Override
  @Transactional
  public SalMetadataRecord create(CreateSalRequest req, String subject, InputStream bytes) throws IOException {
    entitlements.assertAllowed(subject, "UPLOAD_NEW", null, null, Map.of("ownerId", req.getOwnerId(), "salType", req.getSalType().name()));
    String salUuid = UUID.randomUUID().toString();
    long version = 1L;

    SalMetadataRecord r = buildRecord(salUuid, version, req, SalStatus.PENDING_UPLOAD, true, "C");
    if (r.getSalMetadataJson() != null && !r.getSalMetadataJson().isBlank()) JsonUtil.requireValidJsonObject(r.getSalMetadataJson());

    repo.insert(r);
    histRepo.insertSnapshot("C", req.getLstModUser(), "create", r);

    try {
      handlers.get(r.getSalType()).saveStream(r, bytes);
      r.setStatus(SalStatus.AVAILABLE);
      r.setLstModChgCd("U");
      r.setLstModTs(Instant.now());
      repo.updateStatus(r.getSalUuid(), r.getVersion(), r.getStatus(), r.getLstModUser(), r.getLstModChgCd(), r.getLstModTs());
      histRepo.insertSnapshot("U", req.getLstModUser(), "upload-complete", r);
    } catch (Exception e) {
      r.setStatus(SalStatus.FAILED);
      r.setLstModChgCd("U");
      r.setLstModTs(Instant.now());
      repo.updateStatus(r.getSalUuid(), r.getVersion(), r.getStatus(), r.getLstModUser(), r.getLstModChgCd(), r.getLstModTs());
      histRepo.insertSnapshot("U", req.getLstModUser(), "upload-failed: " + e.getMessage(), r);
      throw e;
    }
    return r;
  }

  @Override
  @Transactional
  public SalMetadataRecord createNewVersion(String salUuid, CreateSalRequest req, String subject, InputStream bytes) throws IOException {
    entitlements.assertAllowed(subject, "UPLOAD_VERSION", salUuid, null, Map.of("ownerId", req.getOwnerId(), "salType", req.getSalType().name()));
    long version = repo.nextVersionForUpdate(salUuid);
    repo.clearLatest(salUuid);

    SalMetadataRecord r = buildRecord(salUuid, version, req, SalStatus.PENDING_UPLOAD, true, "C");
    if (r.getSalMetadataJson() != null && !r.getSalMetadataJson().isBlank()) JsonUtil.requireValidJsonObject(r.getSalMetadataJson());

    repo.insert(r);
    histRepo.insertSnapshot("C", req.getLstModUser(), "new-version", r);

    try {
      handlers.get(r.getSalType()).saveStream(r, bytes);
      r.setStatus(SalStatus.AVAILABLE);
      r.setLstModChgCd("U");
      r.setLstModTs(Instant.now());
      repo.updateStatus(r.getSalUuid(), r.getVersion(), r.getStatus(), r.getLstModUser(), r.getLstModChgCd(), r.getLstModTs());
      histRepo.insertSnapshot("U", req.getLstModUser(), "upload-complete", r);
    } catch (Exception e) {
      r.setStatus(SalStatus.FAILED);
      r.setLstModChgCd("U");
      r.setLstModTs(Instant.now());
      repo.updateStatus(r.getSalUuid(), r.getVersion(), r.getStatus(), r.getLstModUser(), r.getLstModChgCd(), r.getLstModTs());
      histRepo.insertSnapshot("U", req.getLstModUser(), "upload-failed: " + e.getMessage(), r);
      throw e;
    }
    return r;
  }

  @Override
  public SalMetadataRecord getLatest(String salUuid, String subject) {
    entitlements.assertAllowed(subject, "READ", salUuid, null, Map.of());
    return repo.findLatest(salUuid).orElseThrow(() -> new IllegalArgumentException("Latest not found for sal_uuid=" + salUuid));
  }

  @Override
  public SalMetadataRecord getVersion(String salUuid, long version, String subject) {
    entitlements.assertAllowed(subject, "READ", salUuid, version, Map.of());
    return repo.findByPk(salUuid, version).orElseThrow(() -> new IllegalArgumentException("Not found sal_uuid=" + salUuid + " version=" + version));
  }

  @Override
  public void downloadLatest(String salUuid, String subject, OutputStream out) throws IOException {
    SalMetadataRecord r = getLatest(salUuid, subject);
    handlers.get(r.getSalType()).readStream(r, out);
  }

  @Override
  public void downloadVersion(String salUuid, long version, String subject, OutputStream out) throws IOException {
    SalMetadataRecord r = getVersion(salUuid, version, subject);
    handlers.get(r.getSalType()).readStream(r, out);
  }

  @Override
  public List<SalMetadataRecord> search(Optional<String> name, Optional<String> ownerId,
                                       Optional<LocalDate> from, Optional<LocalDate> to,
                                       Optional<String> type, Optional<Boolean> latestOnly,
                                       int limit, int offset, String subject) {
    entitlements.assertAllowed(subject, "SEARCH", null, null, Map.of("ownerId", ownerId.orElse(null)));
    Optional<SalType> t = type.map(String::toUpperCase).map(SalType::valueOf);
    return repo.search(name, ownerId, from, to, t, latestOnly, limit, offset);
  }

  private SalMetadataRecord buildRecord(String salUuid, long version, CreateSalRequest req, SalStatus status, boolean latest, String chgCd) {
    SalMetadataRecord r = new SalMetadataRecord();
    r.setSalUuid(salUuid);
    r.setVersion(version);
    r.setSalName(req.getSalName());
    r.setSalDescription(req.getSalDescription());
    r.setSalType(req.getSalType());
    r.setSalMetadataJson(req.getSalMetadataJson());
    r.setStatus(status);
    r.setLatest(latest);
    r.setCompressed(req.isCompressed());
    r.setCompressionType(req.getCompressionType());
    r.setOwnerId(req.getOwnerId());
    r.setLstModChgCd(chgCd);
    r.setLstModUser(req.getLstModUser());
    r.setLstModTs(Instant.now());
    return r;
  }
}
