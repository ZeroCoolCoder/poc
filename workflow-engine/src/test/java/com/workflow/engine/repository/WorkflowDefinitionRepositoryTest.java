package com.workflow.engine.repository;

import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.TransitionDefinition;
import com.workflow.engine.model.definition.WorkflowDefinition;
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
class WorkflowDefinitionRepositoryTest {

    @Autowired
    private WorkflowDefinitionRepository repository;

    private WorkflowDefinition createDefinition(String name, int version, DefinitionStatus status) {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setName(name);
        def.setDescription("Test workflow: " + name);
        def.setVersion(version);
        def.setStatus(status);
        def.setRulesEngineType("spel");
        def.setCreatedAt(LocalDateTime.now());
        def.setUpdatedAt(LocalDateTime.now());
        return def;
    }

    private NodeDefinition createNode(String nodeKey, String name, NodeType type) {
        NodeDefinition node = new NodeDefinition();
        node.setNodeKey(nodeKey);
        node.setName(name);
        node.setNodeType(type);
        node.setDescription("Test node");
        return node;
    }

    private TransitionDefinition createTransition(String source, String target, int priority) {
        TransitionDefinition trans = new TransitionDefinition();
        trans.setName(source + "-to-" + target);
        trans.setSourceNodeKey(source);
        trans.setTargetNodeKey(target);
        trans.setPriority(priority);
        return trans;
    }

    @Test
    void save_shouldInsertNewDefinitionAndAssignId() {
        WorkflowDefinition def = createDefinition("test-wf", 1, DefinitionStatus.DRAFT);
        WorkflowDefinition saved = repository.save(def);
        assertNotNull(saved.getId());
        assertEquals("test-wf", saved.getName());
        assertEquals(1, saved.getVersion());
        assertEquals(DefinitionStatus.DRAFT, saved.getStatus());
    }

    @Test
    void save_shouldUpdateExistingDefinition() {
        WorkflowDefinition def = repository.save(createDefinition("test-wf", 1, DefinitionStatus.DRAFT));
        def.setStatus(DefinitionStatus.ACTIVE);
        def.setUpdatedAt(LocalDateTime.now());
        repository.save(def);

        Optional<WorkflowDefinition> found = repository.findById(def.getId());
        assertTrue(found.isPresent());
        assertEquals(DefinitionStatus.ACTIVE, found.get().getStatus());
    }

    @Test
    void save_shouldCascadeSaveNodesAndTransitions() {
        WorkflowDefinition def = createDefinition("cascade-wf", 1, DefinitionStatus.DRAFT);
        def.addNode(createNode("start", "Start", NodeType.START));
        def.addNode(createNode("end", "End", NodeType.END));
        def.addTransition(createTransition("start", "end", 0));
        def = repository.save(def);

        Optional<WorkflowDefinition> found = repository.findByIdWithNodesAndTransitions(def.getId());
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getNodes().size());
        assertEquals(1, found.get().getTransitions().size());
    }

    @Test
    void findById_shouldReturnEmptyForNonExistent() {
        assertTrue(repository.findById(9999L).isEmpty());
    }

    @Test
    void findByIdWithNodesAndTransitions_shouldReturnFullGraph() {
        WorkflowDefinition def = createDefinition("full-wf", 1, DefinitionStatus.DRAFT);
        def.addNode(createNode("start", "Start", NodeType.START));
        def.addNode(createNode("auto", "Auto", NodeType.AUTO));
        def.addNode(createNode("end", "End", NodeType.END));
        def.addTransition(createTransition("start", "auto", 0));
        def.addTransition(createTransition("auto", "end", 0));
        def = repository.save(def);

        Optional<WorkflowDefinition> found = repository.findByIdWithNodesAndTransitions(def.getId());
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getNodes().size());
        assertEquals(2, found.get().getTransitions().size());
    }

    @Test
    void findByIdWithNodesAndTransitions_shouldReturnEmptyForNonExistent() {
        assertTrue(repository.findByIdWithNodesAndTransitions(9999L).isEmpty());
    }

    @Test
    void findAll_shouldReturnAllDefinitions() {
        repository.save(createDefinition("wf-1", 1, DefinitionStatus.DRAFT));
        repository.save(createDefinition("wf-2", 1, DefinitionStatus.ACTIVE));
        repository.save(createDefinition("wf-3", 1, DefinitionStatus.DEPRECATED));
        assertEquals(3, repository.findAll().size());
    }

    @Test
    void findByStatus_shouldFilterByStatus() {
        repository.save(createDefinition("draft-1", 1, DefinitionStatus.DRAFT));
        repository.save(createDefinition("draft-2", 1, DefinitionStatus.DRAFT));
        repository.save(createDefinition("active-1", 1, DefinitionStatus.ACTIVE));
        assertEquals(2, repository.findByStatus(DefinitionStatus.DRAFT).size());
        assertEquals(1, repository.findByStatus(DefinitionStatus.ACTIVE).size());
    }

    @Test
    void findByName_shouldReturnMatchingDefinitions() {
        repository.save(createDefinition("my-wf", 1, DefinitionStatus.DRAFT));
        repository.save(createDefinition("my-wf", 2, DefinitionStatus.ACTIVE));
        repository.save(createDefinition("other-wf", 1, DefinitionStatus.DRAFT));
        assertEquals(2, repository.findByName("my-wf").size());
    }

    @Test
    void findByNameAndVersion_shouldReturnExactMatch() {
        repository.save(createDefinition("versioned-wf", 1, DefinitionStatus.DRAFT));
        repository.save(createDefinition("versioned-wf", 2, DefinitionStatus.ACTIVE));
        assertTrue(repository.findByNameAndVersion("versioned-wf", 1).isPresent());
        assertTrue(repository.findByNameAndVersion("versioned-wf", 3).isEmpty());
    }

    @Test
    void findByNameOrderByVersionDesc_shouldReturnSortedResults() {
        repository.save(createDefinition("sorted-wf", 1, DefinitionStatus.DRAFT));
        repository.save(createDefinition("sorted-wf", 3, DefinitionStatus.ACTIVE));
        repository.save(createDefinition("sorted-wf", 2, DefinitionStatus.DEPRECATED));

        List<WorkflowDefinition> results = repository.findByNameOrderByVersionDesc("sorted-wf");
        assertEquals(3, results.size());
        assertEquals(3, results.get(0).getVersion());
        assertEquals(2, results.get(1).getVersion());
        assertEquals(1, results.get(2).getVersion());
    }

    @Test
    void save_shouldHandleNodeWithAllFields() {
        WorkflowDefinition def = createDefinition("node-wf", 1, DefinitionStatus.DRAFT);
        NodeDefinition node = createNode("auto", "Auto", NodeType.AUTO);
        node.setActionHandler("dataTransform");
        node.setConfiguration("{\"key\": \"value\"}");
        node.setTimeoutSeconds(30L);
        node.setMaxRetries(3);
        def.addNode(node);
        def = repository.save(def);

        Optional<WorkflowDefinition> found = repository.findByIdWithNodesAndTransitions(def.getId());
        assertTrue(found.isPresent());
        NodeDefinition saved = found.get().getNodes().iterator().next();
        assertEquals("dataTransform", saved.getActionHandler());
        assertEquals(30L, saved.getTimeoutSeconds());
        assertEquals(3, saved.getMaxRetries());
    }

    @Test
    void save_shouldHandleTransitionWithCondition() {
        WorkflowDefinition def = createDefinition("cond-wf", 1, DefinitionStatus.DRAFT);
        def.addNode(createNode("start", "Start", NodeType.START));
        def.addNode(createNode("end", "End", NodeType.END));
        TransitionDefinition trans = createTransition("start", "end", 1);
        trans.setConditionExpression("#amount > 100");
        def.addTransition(trans);
        def = repository.save(def);

        Optional<WorkflowDefinition> found = repository.findByIdWithNodesAndTransitions(def.getId());
        assertTrue(found.isPresent());
        TransitionDefinition savedTrans = found.get().getTransitions().iterator().next();
        assertEquals("#amount > 100", savedTrans.getConditionExpression());
        assertEquals(1, savedTrans.getPriority());
    }

    @Test
    void findByStatus_shouldReturnEmptyListWhenNoMatch() {
        repository.save(createDefinition("draft-only", 1, DefinitionStatus.DRAFT));
        assertEquals(0, repository.findByStatus(DefinitionStatus.DEPRECATED).size());
    }

    @Test
    void findByName_shouldReturnEmptyListWhenNoMatch() {
        assertEquals(0, repository.findByName("nonexistent").size());
    }
}
