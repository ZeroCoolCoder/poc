package com.example.exception.repo;

import com.example.exception.domain.ExceptionRecord;
import com.example.exception.domain.ExceptionType;
import com.example.exception.domain.Severity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExceptionRecordRowMapper implements RowMapper<ExceptionRecord> {

  @Override
  public ExceptionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
    ExceptionRecord r = new ExceptionRecord();
    r.setExceptionCode(rs.getString("exception_code"));
    r.setExceptionType(ExceptionType.valueOf(rs.getString("exception_type")));
    r.setTechnicalDescription(rs.getString("technical_description"));
    r.setBusinessDescription(rs.getString("business_description"));
    r.setHttpStatusCode(rs.getInt("http_status_code"));
    r.setSeverity(Severity.valueOf(rs.getString("severity")));
    r.setActive(rs.getInt("is_active") == 1);
    r.setLstModChgCd(rs.getString("lst_mod_chg_cd"));
    r.setLstModUser(rs.getString("lst_mod_user"));
    r.setLstModTs(rs.getTimestamp("lst_mod_ts").toInstant());
    return r;
  }
}
