package com.workflow.engine.repository;

import com.workflow.engine.model.instance.WorkflowInstance;
import com.workflow.engine.model.instance.WorkflowStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkflowInstanceRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<WorkflowInstance> ROW_MAPPER = (rs, rowNum) -> {
        WorkflowInstance inst = new WorkflowInstance();
        inst.setId(rs.getLong("ID"));
        inst.setWorkflowDefinitionId(rs.getLong("WORKFLOW_DEFINITION_ID"));
        inst.setCorrelationId(rs.getString("CORRELATION_ID"));
        inst.setStatus(WorkflowStatus.valueOf(rs.getString("STATUS")));
        inst.setCurrentNodeKey(rs.getString("CURRENT_NODE_KEY"));
        inst.setContextData(rs.getString("CONTEXT_DATA"));
        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) inst.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");
        if (updatedAt != null) inst.setUpdatedAt(updatedAt.toLocalDateTime());
        Timestamp completedAt = rs.getTimestamp("COMPLETED_AT");
        if (completedAt != null) inst.setCompletedAt(completedAt.toLocalDateTime());
        inst.setCreatedBy(rs.getString("CREATED_BY"));
        long version = rs.getLong("OPT_LOCK_VERSION");
        if (!rs.wasNull()) inst.setOptLockVersion(version);
        return inst;
    };

    public WorkflowInstanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public WorkflowInstance save(WorkflowInstance instance) {
        if (instance.getId() == null) {
            return insert(instance);
        } else {
            return update(instance);
        }
    }

    private WorkflowInstance insert(WorkflowInstance instance) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO WF_INSTANCE (ID, WORKFLOW_DEFINITION_ID, CORRELATION_ID, STATUS, CURRENT_NODE_KEY, CONTEXT_DATA, CREATED_AT, UPDATED_AT, COMPLETED_AT, CREATED_BY, OPT_LOCK_VERSION) " +
                "VALUES (NEXT VALUE FOR WF_INSTANCE_SEQ, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, instance.getWorkflowDefinitionId());
            ps.setString(2, instance.getCorrelationId());
            ps.setString(3, instance.getStatus().name());
            ps.setString(4, instance.getCurrentNodeKey());
            ps.setString(5, instance.getContextData());
            ps.setTimestamp(6, instance.getCreatedAt() != null ? Timestamp.valueOf(instance.getCreatedAt()) : null);
            ps.setTimestamp(7, instance.getUpdatedAt() != null ? Timestamp.valueOf(instance.getUpdatedAt()) : null);
            ps.setTimestamp(8, instance.getCompletedAt() != null ? Timestamp.valueOf(instance.getCompletedAt()) : null);
            ps.setString(9, instance.getCreatedBy());
            ps.setLong(10, instance.getOptLockVersion() != null ? instance.getOptLockVersion() : 0L);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            instance.setId(key.longValue());
        }
        if (instance.getOptLockVersion() == null) {
            instance.setOptLockVersion(0L);
        }
        return instance;
    }

    private WorkflowInstance update(WorkflowInstance instance) {
        long currentVersion = instance.getOptLockVersion() != null ? instance.getOptLockVersion() : 0L;
        int rows = jdbcTemplate.update(
            "UPDATE WF_INSTANCE SET WORKFLOW_DEFINITION_ID = ?, CORRELATION_ID = ?, STATUS = ?, CURRENT_NODE_KEY = ?, " +
            "CONTEXT_DATA = ?, UPDATED_AT = ?, COMPLETED_AT = ?, CREATED_BY = ?, OPT_LOCK_VERSION = ? WHERE ID = ? AND OPT_LOCK_VERSION = ?",
            instance.getWorkflowDefinitionId(), instance.getCorrelationId(), instance.getStatus().name(),
            instance.getCurrentNodeKey(), instance.getContextData(),
            instance.getUpdatedAt() != null ? Timestamp.valueOf(instance.getUpdatedAt()) : null,
            instance.getCompletedAt() != null ? Timestamp.valueOf(instance.getCompletedAt()) : null,
            instance.getCreatedBy(), currentVersion + 1, instance.getId(), currentVersion);
        if (rows > 0) {
            instance.setOptLockVersion(currentVersion + 1);
        }
        return instance;
    }

    public Optional<WorkflowInstance> findById(Long id) {
        List<WorkflowInstance> results = jdbcTemplate.query(
            "SELECT * FROM WF_INSTANCE WHERE ID = ?", ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<WorkflowInstance> findByWorkflowDefinitionId(Long workflowDefinitionId) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_INSTANCE WHERE WORKFLOW_DEFINITION_ID = ?", ROW_MAPPER, workflowDefinitionId);
    }

    public List<WorkflowInstance> findByStatus(WorkflowStatus status) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_INSTANCE WHERE STATUS = ?", ROW_MAPPER, status.name());
    }

    public Optional<WorkflowInstance> findByCorrelationId(String correlationId) {
        List<WorkflowInstance> results = jdbcTemplate.query(
            "SELECT * FROM WF_INSTANCE WHERE CORRELATION_ID = ?", ROW_MAPPER, correlationId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<WorkflowInstance> findByWorkflowDefinitionIdAndStatus(Long workflowDefinitionId, WorkflowStatus status) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_INSTANCE WHERE WORKFLOW_DEFINITION_ID = ? AND STATUS = ?",
            ROW_MAPPER, workflowDefinitionId, status.name());
    }
}
