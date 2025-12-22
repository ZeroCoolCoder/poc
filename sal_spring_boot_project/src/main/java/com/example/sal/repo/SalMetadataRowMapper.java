package com.example.sal.repo;

import com.example.sal.domain.*;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SalMetadataRowMapper implements RowMapper<SalMetadataRecord> {
  @Override
  public SalMetadataRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
    SalMetadataRecord r = new SalMetadataRecord();
    r.setSalUuid(rs.getString("sal_uuid"));
    r.setVersion(rs.getLong("version"));
    r.setSalName(rs.getString("sal_name"));
    r.setSalDescription(rs.getString("sal_description"));
    r.setSalType(SalType.valueOf(rs.getString("sal_type")));
    r.setSalMetadataJson(rs.getString("sal_metadata"));
    long size = rs.getLong("size_in_bytes");
    r.setSizeInBytes(rs.wasNull() ? null : size);
    r.setStatus(SalStatus.valueOf(rs.getString("status")));
    r.setLatest(rs.getInt("is_latest") == 1);
    r.setCompressed(rs.getInt("is_compressed") == 1);
    String ct = rs.getString("compression_type");
    r.setCompressionType(ct == null ? null : CompressionType.valueOf(ct));
    r.setOwnerId(rs.getString("owner_id"));
    r.setLstModChgCd(rs.getString("lst_mod_chg_cd"));
    r.setLstModUser(rs.getString("lst_mod_user"));
    Timestamp ts = rs.getTimestamp("lst_mod_ts");
    r.setLstModTs(ts == null ? null : ts.toInstant());
    return r;
  }
}
