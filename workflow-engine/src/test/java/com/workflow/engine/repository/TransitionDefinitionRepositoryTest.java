package com.workflow.engine.repository;

import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.TransitionDefinition;
import com.workflow.engine.model.definition.WorkflowDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ActiveProfiles("test")
@Import({WorkflowDefinitionRepository.class, NodeDefinitionRepository.class, TransitionDefinitionRepository.class})
class TransitionDefinitionRepositoryTest {

    @Autowired
    private TransitionDefinitionRepository transitionRepository;

    @Autowired
    private WorkflowDefinitionRepository defRepository;

    private Long workflowDefId;

    @BeforeEach
    void setUp() {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setName("trans-test-wf");
        def.setVersion(1);
        def.setStatus(DefinitionStatus.DRAFT);
        def.setRulesEngineType("spel");
        def.setCreatedAt(LocalDateTime.now());

        NodeDefinition start = new NodeDefinition();
        start.setNodeKey("start");
        start.setName("Start");
        start.setNodeType(NodeType.START);
        def.addNode(start);

        NodeDefinition check = new NodeDefinition();
        check.setNodeKey("check");
        check.setName("Check");
        check.setNodeType(NodeType.AUTO);
        def.addNode(check);

        NodeDefinition approved = new NodeDefinition();
        approved.setNodeKey("approved");
        approved.setName("Approved");
        approved.setNodeType(NodeType.AUTO);
        def.addNode(approved);

        NodeDefinition rejected = new NodeDefinition();
        rejected.setNodeKey("rejected");
        rejected.setName("Rejected");
        rejected.setNodeType(NodeType.AUTO);
        def.addNode(rejected);

        NodeDefinition end = new NodeDefinition();
        end.setNodeKey("end");
        end.setName("End");
        end.setNodeType(NodeType.END);
        def.addNode(end);

        TransitionDefinition t1 = new TransitionDefinition();
        t1.setName("start-to-check");
        t1.setSourceNodeKey("start");
        t1.setTargetNodeKey("check");
        t1.setPriority(0);
        def.addTransition(t1);

        TransitionDefinition t2 = new TransitionDefinition();
        t2.setName("check-to-approved");
        t2.setSourceNodeKey("check");
        t2.setTargetNodeKey("approved");
        t2.setConditionExpression("#amount > 100");
        t2.setPriority(1);
        def.addTransition(t2);

        TransitionDefinition t3 = new TransitionDefinition();
        t3.setName("check-to-rejected");
        t3.setSourceNodeKey("check");
        t3.setTargetNodeKey("rejected");
        t3.setPriority(10);
        def.addTransition(t3);

        TransitionDefinition t4 = new TransitionDefinition();
        t4.setName("approved-to-end");
        t4.setSourceNodeKey("approved");
        t4.setTargetNodeKey("end");
        t4.setPriority(0);
        def.addTransition(t4);

        TransitionDefinition t5 = new TransitionDefinition();
        t5.setName("rejected-to-end");
        t5.setSourceNodeKey("rejected");
        t5.setTargetNodeKey("end");
        t5.setPriority(0);
        def.addTransition(t5);

        def = defRepository.save(def);
        workflowDefId = def.getId();
    }

    @Test
    void findByWorkflowDefinitionId_shouldReturnAllTransitions() {
        List<TransitionDefinition> transitions = transitionRepository.findByWorkflowDefinitionId(workflowDefId);
        assertEquals(5, transitions.size());
    }

    @Test
    void findByWorkflowDefinitionId_shouldReturnEmptyForNonExistent() {
        assertTrue(transitionRepository.findByWorkflowDefinitionId(9999L).isEmpty());
    }

    @Test
    void findBySourceNodeKey_shouldReturnOrderedByPriority() {
        List<TransitionDefinition> results = transitionRepository
                .findByWorkflowDefinitionIdAndSourceNodeKeyOrderByPriorityAsc(workflowDefId, "check");
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getPriority());
        assertEquals(10, results.get(1).getPriority());
        assertEquals("check-to-approved", results.get(0).getName());
        assertEquals("check-to-rejected", results.get(1).getName());
    }

    @Test
    void findBySourceNodeKey_shouldReturnEmptyForNonExistentSource() {
        assertTrue(transitionRepository
                .findByWorkflowDefinitionIdAndSourceNodeKeyOrderByPriorityAsc(workflowDefId, "nonexistent")
                .isEmpty());
    }

    @Test
    void findBySourceNodeKey_shouldReturnSingleTransition() {
        List<TransitionDefinition> results = transitionRepository
                .findByWorkflowDefinitionIdAndSourceNodeKeyOrderByPriorityAsc(workflowDefId, "start");
        assertEquals(1, results.size());
        assertEquals("start-to-check", results.get(0).getName());
    }

    @Test
    void transitions_shouldHaveCorrectConditionExpressions() {
        List<TransitionDefinition> results = transitionRepository
                .findByWorkflowDefinitionIdAndSourceNodeKeyOrderByPriorityAsc(workflowDefId, "check");
        TransitionDefinition approved = results.get(0);
        assertEquals("#amount > 100", approved.getConditionExpression());

        TransitionDefinition rejected = results.get(1);
        assertNull(rejected.getConditionExpression());
    }
}
