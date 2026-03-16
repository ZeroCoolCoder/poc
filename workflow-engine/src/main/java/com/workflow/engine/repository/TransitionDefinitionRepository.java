package com.workflow.engine.repository;

import com.workflow.engine.model.definition.TransitionDefinition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransitionDefinitionRepository {

    private final JdbcTemplate jdbcTemplate;

    static final RowMapper<TransitionDefinition> ROW_MAPPER = (rs, rowNum) -> {
        TransitionDefinition trans = new TransitionDefinition();
        trans.setId(rs.getLong("ID"));
        trans.setWorkflowDefinitionId(rs.getLong("WORKFLOW_DEFINITION_ID"));
        trans.setName(rs.getString("NAME"));
        trans.setSourceNodeKey(rs.getString("SOURCE_NODE_KEY"));
        trans.setTargetNodeKey(rs.getString("TARGET_NODE_KEY"));
        trans.setConditionExpression(rs.getString("CONDITION_EXPRESSION"));
        int priority = rs.getInt("PRIORITY");
        if (!rs.wasNull()) trans.setPriority(priority);
        return trans;
    };

    public TransitionDefinitionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TransitionDefinition> findByWorkflowDefinitionId(Long workflowDefinitionId) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_TRANSITION_DEFINITION WHERE WORKFLOW_DEFINITION_ID = ?",
            ROW_MAPPER, workflowDefinitionId);
    }

    public List<TransitionDefinition> findByWorkflowDefinitionIdAndSourceNodeKeyOrderByPriorityAsc(
            Long workflowDefinitionId, String sourceNodeKey) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_TRANSITION_DEFINITION WHERE WORKFLOW_DEFINITION_ID = ? AND SOURCE_NODE_KEY = ? ORDER BY PRIORITY ASC",
            ROW_MAPPER, workflowDefinitionId, sourceNodeKey);
    }
}
