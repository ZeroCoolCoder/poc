package com.example.exception.service;

import com.example.exception.api.CreateExceptionRequest;
import com.example.exception.api.UpdateExceptionRequest;
import com.example.exception.domain.ExceptionRecord;
import com.example.exception.domain.ExceptionType;
import com.example.exception.domain.Severity;
import com.example.exception.repo.ExceptionHistRepository;
import com.example.exception.repo.ExceptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ExceptionServiceImpl implements ExceptionService {

  private final ExceptionRepository repo;
  private final ExceptionHistRepository histRepo;
  private final ExceptionCacheService cacheService;

  public ExceptionServiceImpl(ExceptionRepository repo,
                              ExceptionHistRepository histRepo,
                              ExceptionCacheService cacheService) {
    this.repo = repo;
    this.histRepo = histRepo;
    this.cacheService = cacheService;
  }

  @Override
  @Transactional
  public ExceptionRecord create(CreateExceptionRequest req) {
    repo.findByCode(req.getExceptionCode()).ifPresent(existing -> {
      throw new IllegalArgumentException("Exception code already exists: " + req.getExceptionCode());
    });

    ExceptionRecord r = new ExceptionRecord();
    r.setExceptionCode(req.getExceptionCode());
    r.setExceptionType(req.getExceptionType());
    r.setTechnicalDescription(req.getTechnicalDescription());
    r.setBusinessDescription(req.getBusinessDescription());
    r.setHttpStatusCode(req.getHttpStatusCode());
    r.setSeverity(req.getSeverity());
    r.setActive(true);
    r.setLstModChgCd("C");
    r.setLstModUser(req.getLstModUser());
    r.setLstModTs(Instant.now());

    repo.insert(r);
    histRepo.insertSnapshot("C", req.getLstModUser(), "create", r);
    cacheService.put(r);

    return r;
  }

  @Override
  public ExceptionRecord getByCode(String exceptionCode) {
    ExceptionRecord cached = cacheService.get(exceptionCode);
    if (cached != null) {
      return cached;
    }

    ExceptionRecord r = repo.findByCode(exceptionCode)
        .orElseThrow(() -> new NoSuchElementException("Exception not found: " + exceptionCode));
    cacheService.put(r);
    return r;
  }

  @Override
  @Transactional
  public ExceptionRecord update(String exceptionCode, UpdateExceptionRequest req) {
    ExceptionRecord existing = repo.findByCode(exceptionCode)
        .orElseThrow(() -> new NoSuchElementException("Exception not found: " + exceptionCode));

    if (req.getExceptionType() != null) {
      existing.setExceptionType(req.getExceptionType());
    }
    if (req.getTechnicalDescription() != null) {
      existing.setTechnicalDescription(req.getTechnicalDescription());
    }
    if (req.getBusinessDescription() != null) {
      existing.setBusinessDescription(req.getBusinessDescription());
    }
    if (req.getHttpStatusCode() != null) {
      existing.setHttpStatusCode(req.getHttpStatusCode());
    }
    if (req.getSeverity() != null) {
      existing.setSeverity(req.getSeverity());
    }
    existing.setLstModChgCd("U");
    existing.setLstModUser(req.getLstModUser());
    existing.setLstModTs(Instant.now());

    repo.update(existing);
    histRepo.insertSnapshot("U", req.getLstModUser(), "update", existing);
    cacheService.put(existing);

    return existing;
  }

  @Override
  @Transactional
  public void delete(String exceptionCode, String lstModUser) {
    ExceptionRecord existing = repo.findByCode(exceptionCode)
        .orElseThrow(() -> new NoSuchElementException("Exception not found: " + exceptionCode));

    Instant now = Instant.now();
    repo.softDelete(exceptionCode, lstModUser, now);

    existing.setActive(false);
    existing.setLstModChgCd("D");
    existing.setLstModUser(lstModUser);
    existing.setLstModTs(now);
    histRepo.insertSnapshot("D", lstModUser, "soft-delete", existing);
    cacheService.evict(exceptionCode);
  }

  @Override
  public List<ExceptionRecord> search(Optional<String> type, Optional<String> severity,
                                      Optional<Boolean> activeOnly, int limit, int offset) {
    Optional<ExceptionType> t = type.map(String::toUpperCase).map(ExceptionType::valueOf);
    Optional<Severity> s = severity.map(String::toUpperCase).map(Severity::valueOf);
    return repo.search(t, s, activeOnly, limit, offset);
  }
}
