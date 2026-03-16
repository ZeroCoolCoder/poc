package com.workflow.engine.repository;

import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NodeDefinitionRepository {

    private final JdbcTemplate jdbcTemplate;

    static final RowMapper<NodeDefinition> ROW_MAPPER = (rs, rowNum) -> {
        NodeDefinition node = new NodeDefinition();
        node.setId(rs.getLong("ID"));
        node.setWorkflowDefinitionId(rs.getLong("WORKFLOW_DEFINITION_ID"));
        node.setNodeKey(rs.getString("NODE_KEY"));
        node.setName(rs.getString("NAME"));
        node.setDescription(rs.getString("DESCRIPTION"));
        node.setNodeType(NodeType.valueOf(rs.getString("NODE_TYPE")));
        node.setActionHandler(rs.getString("ACTION_HANDLER"));
        node.setConfiguration(rs.getString("CONFIGURATION"));
        long timeout = rs.getLong("TIMEOUT_SECONDS");
        if (!rs.wasNull()) node.setTimeoutSeconds(timeout);
        int retries = rs.getInt("MAX_RETRIES");
        if (!rs.wasNull()) node.setMaxRetries(retries);
        return node;
    };

    public NodeDefinitionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NodeDefinition> findByWorkflowDefinitionId(Long workflowDefinitionId) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_NODE_DEFINITION WHERE WORKFLOW_DEFINITION_ID = ?",
            ROW_MAPPER, workflowDefinitionId);
    }

    public Optional<NodeDefinition> findByWorkflowDefinitionIdAndNodeKey(Long workflowDefinitionId, String nodeKey) {
        List<NodeDefinition> results = jdbcTemplate.query(
            "SELECT * FROM WF_NODE_DEFINITION WHERE WORKFLOW_DEFINITION_ID = ? AND NODE_KEY = ?",
            ROW_MAPPER, workflowDefinitionId, nodeKey);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<NodeDefinition> findByWorkflowDefinitionIdAndNodeType(Long workflowDefinitionId, NodeType nodeType) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_NODE_DEFINITION WHERE WORKFLOW_DEFINITION_ID = ? AND NODE_TYPE = ?",
            ROW_MAPPER, workflowDefinitionId, nodeType.name());
    }
}
