package com.workflow.engine.repository;

import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.WorkflowDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ActiveProfiles("test")
@Import({WorkflowDefinitionRepository.class, NodeDefinitionRepository.class, TransitionDefinitionRepository.class})
class NodeDefinitionRepositoryTest {

    @Autowired
    private NodeDefinitionRepository nodeRepository;

    @Autowired
    private WorkflowDefinitionRepository defRepository;

    private Long workflowDefId;

    @BeforeEach
    void setUp() {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setName("test-wf");
        def.setVersion(1);
        def.setStatus(DefinitionStatus.DRAFT);
        def.setRulesEngineType("spel");
        def.setCreatedAt(LocalDateTime.now());

        NodeDefinition start = new NodeDefinition();
        start.setNodeKey("start");
        start.setName("Start");
        start.setNodeType(NodeType.START);
        start.setDescription("Start node");
        def.addNode(start);

        NodeDefinition auto = new NodeDefinition();
        auto.setNodeKey("process");
        auto.setName("Process");
        auto.setNodeType(NodeType.AUTO);
        auto.setDescription("Auto node");
        auto.setActionHandler("log");
        def.addNode(auto);

        NodeDefinition waitNode = new NodeDefinition();
        waitNode.setNodeKey("approve");
        waitNode.setName("Approve");
        waitNode.setNodeType(NodeType.WAIT_FOR_INPUT);
        waitNode.setDescription("Wait node");
        def.addNode(waitNode);

        NodeDefinition end = new NodeDefinition();
        end.setNodeKey("end");
        end.setName("End");
        end.setNodeType(NodeType.END);
        end.setDescription("End node");
        def.addNode(end);

        def = defRepository.save(def);
        workflowDefId = def.getId();
    }

    @Test
    void findByWorkflowDefinitionId_shouldReturnAllNodes() {
        List<NodeDefinition> nodes = nodeRepository.findByWorkflowDefinitionId(workflowDefId);
        assertEquals(4, nodes.size());
    }

    @Test
    void findByWorkflowDefinitionId_shouldReturnEmptyForNonExistent() {
        assertTrue(nodeRepository.findByWorkflowDefinitionId(9999L).isEmpty());
    }

    @Test
    void findByWorkflowDefinitionIdAndNodeKey_shouldReturnCorrectNode() {
        Optional<NodeDefinition> result = nodeRepository.findByWorkflowDefinitionIdAndNodeKey(workflowDefId, "start");
        assertTrue(result.isPresent());
        assertEquals("start", result.get().getNodeKey());
        assertEquals(NodeType.START, result.get().getNodeType());
    }

    @Test
    void findByWorkflowDefinitionIdAndNodeKey_shouldReturnEmptyForNonExistent() {
        assertTrue(nodeRepository.findByWorkflowDefinitionIdAndNodeKey(workflowDefId, "nonexistent").isEmpty());
    }

    @Test
    void findByWorkflowDefinitionIdAndNodeType_shouldFilterByType() {
        List<NodeDefinition> startNodes = nodeRepository.findByWorkflowDefinitionIdAndNodeType(workflowDefId, NodeType.START);
        assertEquals(1, startNodes.size());
        assertEquals("start", startNodes.get(0).getNodeKey());

        List<NodeDefinition> autoNodes = nodeRepository.findByWorkflowDefinitionIdAndNodeType(workflowDefId, NodeType.AUTO);
        assertEquals(1, autoNodes.size());

        List<NodeDefinition> waitNodes = nodeRepository.findByWorkflowDefinitionIdAndNodeType(workflowDefId, NodeType.WAIT_FOR_INPUT);
        assertEquals(1, waitNodes.size());

        List<NodeDefinition> forkNodes = nodeRepository.findByWorkflowDefinitionIdAndNodeType(workflowDefId, NodeType.FORK);
        assertTrue(forkNodes.isEmpty());
    }

    @Test
    void findByWorkflowDefinitionIdAndNodeKey_shouldReturnNodeWithAllFields() {
        Optional<NodeDefinition> result = nodeRepository.findByWorkflowDefinitionIdAndNodeKey(workflowDefId, "process");
        assertTrue(result.isPresent());
        NodeDefinition node = result.get();
        assertNotNull(node.getId());
        assertEquals(workflowDefId, node.getWorkflowDefinitionId());
        assertEquals("process", node.getNodeKey());
        assertEquals("Process", node.getName());
        assertEquals(NodeType.AUTO, node.getNodeType());
        assertEquals("log", node.getActionHandler());
    }
}
