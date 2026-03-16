package com.workflow.engine.repository;

import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.TransitionDefinition;
import com.workflow.engine.model.definition.WorkflowDefinition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkflowDefinitionRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<WorkflowDefinition> DEFINITION_ROW_MAPPER = (rs, rowNum) -> {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setId(rs.getLong("ID"));
        def.setName(rs.getString("NAME"));
        def.setDescription(rs.getString("DESCRIPTION"));
        def.setVersion(rs.getInt("VERSION"));
        def.setStatus(DefinitionStatus.valueOf(rs.getString("STATUS")));
        def.setRulesEngineType(rs.getString("RULES_ENGINE_TYPE"));
        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) def.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");
        if (updatedAt != null) def.setUpdatedAt(updatedAt.toLocalDateTime());
        return def;
    };

    static final RowMapper<NodeDefinition> NODE_ROW_MAPPER = (rs, rowNum) -> {
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

    static final RowMapper<TransitionDefinition> TRANSITION_ROW_MAPPER = (rs, rowNum) -> {
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

    public WorkflowDefinitionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public WorkflowDefinition save(WorkflowDefinition definition) {
        if (definition.getId() == null) {
            return insert(definition);
        } else {
            return update(definition);
        }
    }

    private WorkflowDefinition insert(WorkflowDefinition definition) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO WF_DEFINITION (ID, NAME, DESCRIPTION, VERSION, STATUS, RULES_ENGINE_TYPE, CREATED_AT, UPDATED_AT) " +
                "VALUES (NEXT VALUE FOR WF_DEFINITION_SEQ, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, definition.getName());
            ps.setString(2, definition.getDescription());
            ps.setInt(3, definition.getVersion());
            ps.setString(4, definition.getStatus().name());
            ps.setString(5, definition.getRulesEngineType());
            ps.setTimestamp(6, Timestamp.valueOf(definition.getCreatedAt()));
            ps.setTimestamp(7, definition.getUpdatedAt() != null ? Timestamp.valueOf(definition.getUpdatedAt()) : null);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            definition.setId(key.longValue());
        }

        for (NodeDefinition node : definition.getNodes()) {
            node.setWorkflowDefinitionId(definition.getId());
            saveNode(node);
        }

        for (TransitionDefinition transition : definition.getTransitions()) {
            transition.setWorkflowDefinitionId(definition.getId());
            saveTransition(transition);
        }

        return definition;
    }

    private WorkflowDefinition update(WorkflowDefinition definition) {
        jdbcTemplate.update(
            "UPDATE WF_DEFINITION SET NAME = ?, DESCRIPTION = ?, VERSION = ?, STATUS = ?, RULES_ENGINE_TYPE = ?, UPDATED_AT = ? WHERE ID = ?",
            definition.getName(), definition.getDescription(), definition.getVersion(),
            definition.getStatus().name(), definition.getRulesEngineType(),
            definition.getUpdatedAt() != null ? Timestamp.valueOf(definition.getUpdatedAt()) : null,
            definition.getId());
        return definition;
    }

    private void saveNode(NodeDefinition node) {
        if (node.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO WF_NODE_DEFINITION (ID, WORKFLOW_DEFINITION_ID, NODE_KEY, NAME, DESCRIPTION, NODE_TYPE, ACTION_HANDLER, CONFIGURATION, TIMEOUT_SECONDS, MAX_RETRIES) " +
                    "VALUES (NEXT VALUE FOR WF_NODE_DEF_SEQ, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, node.getWorkflowDefinitionId());
                ps.setString(2, node.getNodeKey());
                ps.setString(3, node.getName());
                ps.setString(4, node.getDescription());
                ps.setString(5, node.getNodeType().name());
                ps.setString(6, node.getActionHandler());
                ps.setString(7, node.getConfiguration());
                if (node.getTimeoutSeconds() != null) {
                    ps.setLong(8, node.getTimeoutSeconds());
                } else {
                    ps.setNull(8, java.sql.Types.BIGINT);
                }
                if (node.getMaxRetries() != null) {
                    ps.setInt(9, node.getMaxRetries());
                } else {
                    ps.setNull(9, java.sql.Types.INTEGER);
                }
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                node.setId(key.longValue());
            }
        }
    }

    private void saveTransition(TransitionDefinition transition) {
        if (transition.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO WF_TRANSITION_DEFINITION (ID, WORKFLOW_DEFINITION_ID, NAME, SOURCE_NODE_KEY, TARGET_NODE_KEY, CONDITION_EXPRESSION, PRIORITY) " +
                    "VALUES (NEXT VALUE FOR WF_TRANS_DEF_SEQ, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, transition.getWorkflowDefinitionId());
                ps.setString(2, transition.getName());
                ps.setString(3, transition.getSourceNodeKey());
                ps.setString(4, transition.getTargetNodeKey());
                ps.setString(5, transition.getConditionExpression());
                if (transition.getPriority() != null) {
                    ps.setInt(6, transition.getPriority());
                } else {
                    ps.setInt(6, 0);
                }
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                transition.setId(key.longValue());
            }
        }
    }

    public Optional<WorkflowDefinition> findById(Long id) {
        List<WorkflowDefinition> results = jdbcTemplate.query(
            "SELECT * FROM WF_DEFINITION WHERE ID = ?", DEFINITION_ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<WorkflowDefinition> findByIdWithNodesAndTransitions(Long id) {
        Optional<WorkflowDefinition> optDef = findById(id);
        if (optDef.isEmpty()) return Optional.empty();

        WorkflowDefinition def = optDef.get();
        List<NodeDefinition> nodes = jdbcTemplate.query(
            "SELECT * FROM WF_NODE_DEFINITION WHERE WORKFLOW_DEFINITION_ID = ?", NODE_ROW_MAPPER, id);
        def.setNodes(new HashSet<>(nodes));

        List<TransitionDefinition> transitions = jdbcTemplate.query(
            "SELECT * FROM WF_TRANSITION_DEFINITION WHERE WORKFLOW_DEFINITION_ID = ?", TRANSITION_ROW_MAPPER, id);
        def.setTransitions(new HashSet<>(transitions));

        return Optional.of(def);
    }

    public List<WorkflowDefinition> findAll() {
        return jdbcTemplate.query("SELECT * FROM WF_DEFINITION", DEFINITION_ROW_MAPPER);
    }

    public List<WorkflowDefinition> findByStatus(DefinitionStatus status) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_DEFINITION WHERE STATUS = ?", DEFINITION_ROW_MAPPER, status.name());
    }

    public List<WorkflowDefinition> findByName(String name) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_DEFINITION WHERE NAME = ?", DEFINITION_ROW_MAPPER, name);
    }

    public Optional<WorkflowDefinition> findByNameAndVersion(String name, Integer version) {
        List<WorkflowDefinition> results = jdbcTemplate.query(
            "SELECT * FROM WF_DEFINITION WHERE NAME = ? AND VERSION = ?", DEFINITION_ROW_MAPPER, name, version);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<WorkflowDefinition> findByNameOrderByVersionDesc(String name) {
        return jdbcTemplate.query(
            "SELECT * FROM WF_DEFINITION WHERE NAME = ? ORDER BY VERSION DESC", DEFINITION_ROW_MAPPER, name);
    }
}
