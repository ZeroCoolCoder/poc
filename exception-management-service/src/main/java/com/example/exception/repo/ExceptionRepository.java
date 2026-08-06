package com.example.exception.repo;

import com.example.exception.domain.ExceptionRecord;
import com.example.exception.domain.ExceptionType;
import com.example.exception.domain.Severity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ExceptionRepository {

  private final JdbcTemplate jdbc;

  public ExceptionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Transactional
  public void insert(ExceptionRecord r) {
    String sql =
        "INSERT INTO exception_definition " +
        "(exception_code, exception_type, technical_description, business_description, " +
        " http_status_code, severity, is_active, lst_mod_chg_cd, lst_mod_user, lst_mod_ts) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    jdbc.update(sql,
        r.getExceptionCode(), r.getExceptionType().name(),
        r.getTechnicalDescription(), r.getBusinessDescription(),
        r.getHttpStatusCode(), r.getSeverity().name(),
        r.isActive() ? 1 : 0,
        r.getLstModChgCd(), r.getLstModUser(), Timestamp.from(r.getLstModTs()));
  }

  public Optional<ExceptionRecord> findByCode(String exceptionCode) {
    List<ExceptionRecord> rows = jdbc.query(
        "SELECT * FROM exception_definition WHERE exception_code = ?",
        new ExceptionRecordRowMapper(), exceptionCode);
    return rows.stream().findFirst();
  }

  public Optional<ExceptionRecord> findActiveByCode(String exceptionCode) {
    List<ExceptionRecord> rows = jdbc.query(
        "SELECT * FROM exception_definition WHERE exception_code = ? AND is_active = 1",
        new ExceptionRecordRowMapper(), exceptionCode);
    return rows.stream().findFirst();
  }

  @Transactional
  public int update(ExceptionRecord r) {
    String sql =
        "UPDATE exception_definition SET " +
        "exception_type = ?, technical_description = ?, business_description = ?, " +
        "http_status_code = ?, severity = ?, is_active = ?, " +
        "lst_mod_chg_cd = ?, lst_mod_user = ?, lst_mod_ts = ? " +
        "WHERE exception_code = ?";
    return jdbc.update(sql,
        r.getExceptionType().name(),
        r.getTechnicalDescription(), r.getBusinessDescription(),
        r.getHttpStatusCode(), r.getSeverity().name(),
        r.isActive() ? 1 : 0,
        r.getLstModChgCd(), r.getLstModUser(), Timestamp.from(r.getLstModTs()),
        r.getExceptionCode());
  }

  @Transactional
  public int softDelete(String exceptionCode, String lstModUser, Instant lstModTs) {
    return jdbc.update(
        "UPDATE exception_definition SET is_active = 0, lst_mod_chg_cd = 'D', " +
        "lst_mod_user = ?, lst_mod_ts = ? WHERE exception_code = ?",
        lstModUser, Timestamp.from(lstModTs), exceptionCode);
  }

  public List<ExceptionRecord> search(Optional<ExceptionType> type, Optional<Severity> severity,
                                      Optional<Boolean> activeOnly, int limit, int offset) {
    StringBuilder sb = new StringBuilder("SELECT * FROM exception_definition WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (type.isPresent()) {
      sb.append(" AND exception_type = ?");
      params.add(type.get().name());
    }
    if (severity.isPresent()) {
      sb.append(" AND severity = ?");
      params.add(severity.get().name());
    }
    if (activeOnly.isPresent() && activeOnly.get()) {
      sb.append(" AND is_active = 1");
    }

    sb.append(" ORDER BY exception_code ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    params.add(offset);
    params.add(limit);

    return jdbc.query(sb.toString(), new ExceptionRecordRowMapper(), params.toArray());
  }
}
