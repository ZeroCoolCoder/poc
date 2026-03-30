package com.example.exception.repo;

import com.example.exception.domain.ExceptionRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
public class ExceptionHistRepository {

  private final JdbcTemplate jdbc;

  public ExceptionHistRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Transactional
  public void insertSnapshot(String histAction, String histUser, String histReason, ExceptionRecord r) {
    String sql =
        "INSERT INTO exception_definition_hist " +
        "(hist_ts, hist_action, hist_user, hist_reason, " +
        " exception_code, exception_type, technical_description, business_description, " +
        " http_status_code, severity, is_active, lst_mod_chg_cd, lst_mod_user, lst_mod_ts) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    jdbc.update(sql,
        Timestamp.from(Instant.now()), histAction, histUser, histReason,
        r.getExceptionCode(), r.getExceptionType().name(),
        r.getTechnicalDescription(), r.getBusinessDescription(),
        r.getHttpStatusCode(), r.getSeverity().name(),
        r.isActive() ? 1 : 0,
        r.getLstModChgCd(), r.getLstModUser(), Timestamp.from(r.getLstModTs()));
  }
}
