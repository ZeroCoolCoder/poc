package com.workflow.engine.repository;

import com.workflow.engine.model.instance.TransitionLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class TransitionLogRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<TransitionLog> ROW_MAPPER = (rs, rowNum) -> {
        TransitionLog log = new TransitionLog();
        log.setId(rs.getLong("ID"));
        log.setWorkflowInstanceId(rs.getLong("WORKFLOW_INSTANCE_ID"));
        long transDefId = rs.getLong("TRANSITION_DEFINITION_ID");
        if (!rs.wasNull()) log.setTransitionDefinitionId(transDefId);
        log.setFromNodeKey(rs.getString("FROM_NODE_KEY"));
        log.setToNodeKey(rs.getString("TO_NODE_KEY"));
        log.setConditionResult(rs.getString("CONDITION_RESULT"));
        Timestamp transitionedAt = rs.getTimestamp("TRANSITIONED_AT");
        if (transitionedAt != null) log.setTransitionedAt(transitionedAt.toLocalDateTime());
        return log;
    };

    public TransitionLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TransitionLog save(TransitionLog transitionLog) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO WF_TRANSITION_LOG (ID, WORKFLOW_INSTANCE_ID, TRANSITION_DEFINITION_ID, FROM_NODE_KEY, TO_NODE_KEY, CONDITION_RESULT, TRANSITIONED_AT) " +
                "VALUES (NEXT VALUE FOR WF_TRANS_LOG_SEQ, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, transitionLog.getWorkflowInstanceId());
            if (transitionLog.getTransitionDefinitionId() != null) {
                ps.setLong(2, transitionLog.getTransitionDefinitionId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, transitionLog.getFromNodeKey());
            ps.setString(4, transitionLog.getToNodeKey());
            ps.setString(5, transitionLog.getConditionResult());
            ps.setTimestamp(6, Timestamp.valueOf(transitionLog.getTransitionedAt()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            transitionLog.setId(key.longValue());
        }
        return transitionLog;
    }

    public List<TransitionLog> findByWorkflowInstanceIdOrderByTransitionedAtAsc(Long workflowInstanceId) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_TRANSITION_LOG WHERE WORKFLOW_INSTANCE_ID = ? ORDER BY TRANSITIONED_AT ASC",
            ROW_MAPPER, workflowInstanceId);
    }
}
