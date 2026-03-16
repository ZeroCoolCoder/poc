package com.workflow.engine.repository;

import com.workflow.engine.model.instance.NodeExecution;
import com.workflow.engine.model.instance.NodeExecutionStatus;
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
public class NodeExecutionRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<NodeExecution> ROW_MAPPER = (rs, rowNum) -> {
        NodeExecution exec = new NodeExecution();
        exec.setId(rs.getLong("ID"));
        exec.setWorkflowInstanceId(rs.getLong("WORKFLOW_INSTANCE_ID"));
        exec.setNodeDefinitionId(rs.getLong("NODE_DEFINITION_ID"));
        exec.setNodeKey(rs.getString("NODE_KEY"));
        exec.setStatus(NodeExecutionStatus.valueOf(rs.getString("STATUS")));
        exec.setInputData(rs.getString("INPUT_DATA"));
        exec.setOutputData(rs.getString("OUTPUT_DATA"));
        exec.setErrorMessage(rs.getString("ERROR_MESSAGE"));
        int retryCount = rs.getInt("RETRY_COUNT");
        if (!rs.wasNull()) exec.setRetryCount(retryCount);
        Timestamp startedAt = rs.getTimestamp("STARTED_AT");
        if (startedAt != null) exec.setStartedAt(startedAt.toLocalDateTime());
        Timestamp completedAt = rs.getTimestamp("COMPLETED_AT");
        if (completedAt != null) exec.setCompletedAt(completedAt.toLocalDateTime());
        return exec;
    };

    public NodeExecutionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public NodeExecution save(NodeExecution execution) {
        if (execution.getId() == null) {
            return insert(execution);
        } else {
            return update(execution);
        }
    }

    private NodeExecution insert(NodeExecution execution) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO WF_NODE_EXECUTION (ID, WORKFLOW_INSTANCE_ID, NODE_DEFINITION_ID, NODE_KEY, STATUS, INPUT_DATA, OUTPUT_DATA, ERROR_MESSAGE, RETRY_COUNT, STARTED_AT, COMPLETED_AT) " +
                "VALUES (NEXT VALUE FOR WF_NODE_EXEC_SEQ, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, execution.getWorkflowInstanceId());
            ps.setLong(2, execution.getNodeDefinitionId());
            ps.setString(3, execution.getNodeKey());
            ps.setString(4, execution.getStatus().name());
            ps.setString(5, execution.getInputData());
            ps.setString(6, execution.getOutputData());
            ps.setString(7, execution.getErrorMessage());
            if (execution.getRetryCount() != null) {
                ps.setInt(8, execution.getRetryCount());
            } else {
                ps.setInt(8, 0);
            }
            ps.setTimestamp(9, execution.getStartedAt() != null ? Timestamp.valueOf(execution.getStartedAt()) : null);
            ps.setTimestamp(10, execution.getCompletedAt() != null ? Timestamp.valueOf(execution.getCompletedAt()) : null);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            execution.setId(key.longValue());
        }
        return execution;
    }

    private NodeExecution update(NodeExecution execution) {
        jdbcTemplate.update(
            "UPDATE WF_NODE_EXECUTION SET STATUS = ?, OUTPUT_DATA = ?, ERROR_MESSAGE = ?, RETRY_COUNT = ?, COMPLETED_AT = ? WHERE ID = ?",
            execution.getStatus().name(),
            execution.getOutputData(),
            execution.getErrorMessage(),
            execution.getRetryCount() != null ? execution.getRetryCount() : 0,
            execution.getCompletedAt() != null ? Timestamp.valueOf(execution.getCompletedAt()) : null,
            execution.getId());
        return execution;
    }

    public List<NodeExecution> findByWorkflowInstanceId(Long workflowInstanceId) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_NODE_EXECUTION WHERE WORKFLOW_INSTANCE_ID = ?",
            ROW_MAPPER, workflowInstanceId);
    }

    public List<NodeExecution> findByWorkflowInstanceIdAndStatus(Long workflowInstanceId, NodeExecutionStatus status) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_NODE_EXECUTION WHERE WORKFLOW_INSTANCE_ID = ? AND STATUS = ?",
            ROW_MAPPER, workflowInstanceId, status.name());
    }

    public List<NodeExecution> findByWorkflowInstanceIdAndNodeKey(Long workflowInstanceId, String nodeKey) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_NODE_EXECUTION WHERE WORKFLOW_INSTANCE_ID = ? AND NODE_KEY = ?",
            ROW_MAPPER, workflowInstanceId, nodeKey);
    }

    public List<NodeExecution> findByWorkflowInstanceIdOrderByStartedAtAsc(Long workflowInstanceId) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_NODE_EXECUTION WHERE WORKFLOW_INSTANCE_ID = ? ORDER BY STARTED_AT ASC",
            ROW_MAPPER, workflowInstanceId);
    }
}
