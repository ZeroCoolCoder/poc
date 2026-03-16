package com.workflow.engine.repository;

import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.TransitionDefinition;
import com.workflow.engine.model.definition.WorkflowDefinition;
import com.workflow.engine.model.instance.TransitionLog;
import com.workflow.engine.model.instance.WorkflowInstance;
import com.workflow.engine.model.instance.WorkflowStatus;
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
@Import({TransitionLogRepository.class, WorkflowInstanceRepository.class,
         WorkflowDefinitionRepository.class, NodeDefinitionRepository.class,
         TransitionDefinitionRepository.class})
class TransitionLogRepositoryTest {

    @Autowired
    private TransitionLogRepository logRepository;

    @Autowired
    private WorkflowInstanceRepository instanceRepository;

    @Autowired
    private WorkflowDefinitionRepository defRepository;

    private Long instanceId;

    @BeforeEach
    void setUp() {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setName("log-test-wf");
        def.setVersion(1);
        def.setStatus(DefinitionStatus.ACTIVE);
        def.setRulesEngineType("spel");
        def.setCreatedAt(LocalDateTime.now());

        NodeDefinition start = new NodeDefinition();
        start.setNodeKey("start");
        start.setName("Start");
        start.setNodeType(NodeType.START);
        def.addNode(start);

        def = defRepository.save(def);

        WorkflowInstance inst = new WorkflowInstance();
        inst.setWorkflowDefinitionId(def.getId());
        inst.setStatus(WorkflowStatus.RUNNING);
        inst.setCurrentNodeKey("start");
        inst.setCreatedAt(LocalDateTime.now());
        inst = instanceRepository.save(inst);
        instanceId = inst.getId();
    }

    @Test
    void save_shouldInsertTransitionLogAndAssignId() {
        TransitionLog log = new TransitionLog();
        log.setWorkflowInstanceId(instanceId);
        log.setFromNodeKey("start");
        log.setToNodeKey("process");
        log.setTransitionedAt(LocalDateTime.now());
        log = logRepository.save(log);

        assertNotNull(log.getId());
    }

    @Test
    void save_shouldHandleNullTransitionDefinitionId() {
        TransitionLog log = new TransitionLog();
        log.setWorkflowInstanceId(instanceId);
        log.setFromNodeKey("start");
        log.setToNodeKey("end");
        log.setTransitionedAt(LocalDateTime.now());
        log = logRepository.save(log);

        assertNotNull(log.getId());
    }

    @Test
    void save_shouldHandleConditionResult() {
        TransitionLog log = new TransitionLog();
        log.setWorkflowInstanceId(instanceId);
        log.setFromNodeKey("check");
        log.setToNodeKey("approved");
        log.setConditionResult("#amount > 100 = true");
        log.setTransitionedAt(LocalDateTime.now());
        log = logRepository.save(log);

        List<TransitionLog> results = logRepository.findByWorkflowInstanceIdOrderByTransitionedAtAsc(instanceId);
        assertEquals(1, results.size());
        assertEquals("#amount > 100 = true", results.get(0).getConditionResult());
    }

    @Test
    void findByWorkflowInstanceIdOrderByTransitionedAtAsc_shouldReturnOrdered() {
        TransitionLog log1 = new TransitionLog();
        log1.setWorkflowInstanceId(instanceId);
        log1.setFromNodeKey("start");
        log1.setToNodeKey("process");
        log1.setTransitionedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        logRepository.save(log1);

        TransitionLog log2 = new TransitionLog();
        log2.setWorkflowInstanceId(instanceId);
        log2.setFromNodeKey("process");
        log2.setToNodeKey("approve");
        log2.setTransitionedAt(LocalDateTime.of(2026, 1, 1, 11, 0));
        logRepository.save(log2);

        TransitionLog log3 = new TransitionLog();
        log3.setWorkflowInstanceId(instanceId);
        log3.setFromNodeKey("approve");
        log3.setToNodeKey("end");
        log3.setTransitionedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        logRepository.save(log3);

        List<TransitionLog> results = logRepository.findByWorkflowInstanceIdOrderByTransitionedAtAsc(instanceId);
        assertEquals(3, results.size());
        assertEquals("start", results.get(0).getFromNodeKey());
        assertEquals("process", results.get(1).getFromNodeKey());
        assertEquals("approve", results.get(2).getFromNodeKey());
    }

    @Test
    void findByWorkflowInstanceIdOrderByTransitionedAtAsc_shouldReturnEmptyForNonExistent() {
        assertTrue(logRepository.findByWorkflowInstanceIdOrderByTransitionedAtAsc(9999L).isEmpty());
    }

    @Test
    void save_shouldPreserveAllFields() {
        TransitionLog log = new TransitionLog();
        log.setWorkflowInstanceId(instanceId);
        log.setTransitionDefinitionId(42L);
        log.setFromNodeKey("start");
        log.setToNodeKey("process");
        log.setConditionResult("matched");
        log.setTransitionedAt(LocalDateTime.of(2026, 3, 15, 12, 0));
        log = logRepository.save(log);

        List<TransitionLog> results = logRepository.findByWorkflowInstanceIdOrderByTransitionedAtAsc(instanceId);
        assertEquals(1, results.size());
        TransitionLog saved = results.get(0);
        assertEquals(instanceId, saved.getWorkflowInstanceId());
        assertEquals(42L, saved.getTransitionDefinitionId());
        assertEquals("start", saved.getFromNodeKey());
        assertEquals("process", saved.getToNodeKey());
        assertEquals("matched", saved.getConditionResult());
    }
}
