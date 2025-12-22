package com.example.sal.repo;

import com.example.sal.domain.SalMetadataRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
public class SalMetadataHistRepository {
  private final JdbcTemplate jdbc;
  public SalMetadataHistRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public void insertSnapshot(String action, String histUser, String histReason, SalMetadataRecord r) {
    String sql =
        "INSERT INTO sal_metadata_hist " +
        "(hist_ts, hist_action, hist_user, hist_reason, " +
        " sal_uuid, version, sal_name, sal_description, sal_type, sal_metadata, size_in_bytes, " +
        " status, is_latest, is_compressed, compression_type, owner_id, lst_mod_chg_cd, lst_mod_user, lst_mod_ts) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    jdbc.update(sql,
        Timestamp.from(Instant.now()), action, histUser, histReason,
        r.getSalUuid(), r.getVersion(), r.getSalName(), r.getSalDescription(), r.getSalType().name(),
        r.getSalMetadataJson(), r.getSizeInBytes(),
        r.getStatus().name(), r.isLatest() ? 1 : 0,
        r.isCompressed() ? 1 : 0, r.getCompressionType() == null ? null : r.getCompressionType().name(),
        r.getOwnerId(), r.getLstModChgCd(), r.getLstModUser(), Timestamp.from(r.getLstModTs()));
  }
}
