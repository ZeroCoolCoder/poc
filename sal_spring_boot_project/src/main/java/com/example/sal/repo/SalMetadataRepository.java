package com.example.sal.repo;

import com.example.sal.domain.SalMetadataRecord;
import com.example.sal.domain.SalStatus;
import com.example.sal.domain.SalType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SalMetadataRepository {
  private final JdbcTemplate jdbc;
  public SalMetadataRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Transactional
  public void insert(SalMetadataRecord r) {
    String sql =
        "INSERT INTO sal_metadata " +
        "(sal_uuid, version, sal_name, sal_description, sal_type, sal_metadata, size_in_bytes, " +
        " status, is_latest, is_compressed, compression_type, owner_id, lst_mod_chg_cd, lst_mod_user, lst_mod_ts) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    jdbc.update(sql,
        r.getSalUuid(), r.getVersion(), r.getSalName(), r.getSalDescription(),
        r.getSalType().name(), r.getSalMetadataJson(), r.getSizeInBytes(),
        r.getStatus().name(), r.isLatest() ? 1 : 0,
        r.isCompressed() ? 1 : 0, r.getCompressionType() == null ? null : r.getCompressionType().name(),
        r.getOwnerId(), r.getLstModChgCd(), r.getLstModUser(), Timestamp.from(r.getLstModTs()));
  }

  public Optional<SalMetadataRecord> findByPk(String salUuid, long version) {
    List<SalMetadataRecord> rows = jdbc.query(
        "SELECT * FROM sal_metadata WHERE sal_uuid=? AND version=?",
        new SalMetadataRowMapper(), salUuid, version);
    return rows.stream().findFirst();
  }

  public Optional<SalMetadataRecord> findLatest(String salUuid) {
    List<SalMetadataRecord> rows = jdbc.query(
        "SELECT * FROM sal_metadata WHERE sal_uuid=? AND is_latest=1",
        new SalMetadataRowMapper(), salUuid);
    return rows.stream().findFirst();
  }

  @Transactional
  public long nextVersionForUpdate(String salUuid) {
    jdbc.query("SELECT version FROM sal_metadata WHERE sal_uuid=? AND is_latest=1 FOR UPDATE", rs -> null, salUuid);
    Long max = jdbc.queryForObject("SELECT MAX(version) FROM sal_metadata WHERE sal_uuid=?", Long.class, salUuid);
    return (max == null ? 0L : max) + 1L;
  }

  @Transactional
  public int clearLatest(String salUuid) {
    return jdbc.update("UPDATE sal_metadata SET is_latest=0 WHERE sal_uuid=? AND is_latest=1", salUuid);
  }

  @Transactional
  public int updateStatus(String salUuid, long version, SalStatus status, String lstModUser, String lstModChgCd, Instant lstModTs) {
    return jdbc.update(
        "UPDATE sal_metadata SET status=?, lst_mod_user=?, lst_mod_chg_cd=?, lst_mod_ts=? WHERE sal_uuid=? AND version=?",
        status.name(), lstModUser, lstModChgCd, Timestamp.from(lstModTs), salUuid, version);
  }

  public List<SalMetadataRecord> search(Optional<String> name, Optional<String> ownerId,
                                       Optional<LocalDate> from, Optional<LocalDate> to,
                                       Optional<SalType> type, Optional<Boolean> latestOnly,
                                       int limit, int offset) {
    StringBuilder sb = new StringBuilder("SELECT * FROM sal_metadata WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (name.isPresent()) {
      sb.append(" AND LOWER(sal_name) LIKE ?");
      params.add("%" + name.get().toLowerCase() + "%");
    }
    if (ownerId.isPresent()) {
      sb.append(" AND owner_id = ?");
      params.add(ownerId.get());
    }
    if (type.isPresent()) {
      sb.append(" AND sal_type = ?");
      params.add(type.get().name());
    }
    if (latestOnly.isPresent() && latestOnly.get()) {
      sb.append(" AND is_latest = 1");
    }
    if (from.isPresent()) {
      sb.append(" AND lst_mod_ts >= ?");
      params.add(Timestamp.valueOf(from.get().atStartOfDay()));
    }
    if (to.isPresent()) {
      sb.append(" AND lst_mod_ts < ?");
      params.add(Timestamp.valueOf(to.get().plusDays(1).atStartOfDay()));
    }

    sb.append(" ORDER BY lst_mod_ts DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
    params.add(offset);
    params.add(limit);

    return jdbc.query(sb.toString(), new SalMetadataRowMapper(), params.toArray());
  }
}
