package com.workflow.engine.repository;

import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.WorkflowDefinition;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ActiveProfiles("test")
@Import({WorkflowInstanceRepository.class, WorkflowDefinitionRepository.class,
         NodeDefinitionRepository.class, TransitionDefinitionRepository.class})
class WorkflowInstanceRepositoryTest {

    @Autowired
    private WorkflowInstanceRepository instanceRepository;

    @Autowired
    private WorkflowDefinitionRepository defRepository;

    private Long workflowDefId;

    @BeforeEach
    void setUp() {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setName("inst-test-wf");
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
        workflowDefId = def.getId();
    }

    private WorkflowInstance createInstance(WorkflowStatus status, String correlationId) {
        WorkflowInstance inst = new WorkflowInstance();
        inst.setWorkflowDefinitionId(workflowDefId);
        inst.setCorrelationId(correlationId);
        inst.setStatus(status);
        inst.setCurrentNodeKey("start");
        inst.setContextData("{\"key\": \"value\"}");
        inst.setCreatedAt(LocalDateTime.now());
        inst.setUpdatedAt(LocalDateTime.now());
        inst.setCreatedBy("test-user");
        return inst;
    }

    @Test
    void save_shouldInsertNewInstanceAndAssignId() {
        WorkflowInstance inst = createInstance(WorkflowStatus.RUNNING, "corr-1");
        inst = instanceRepository.save(inst);
        assertNotNull(inst.getId());
        assertEquals(0L, inst.getOptLockVersion());
    }

    @Test
    void save_shouldUpdateExistingInstanceWithOptimisticLocking() {
        WorkflowInstance inst = instanceRepository.save(createInstance(WorkflowStatus.RUNNING, "corr-lock"));
        assertEquals(0L, inst.getOptLockVersion());

        inst.setStatus(WorkflowStatus.COMPLETED);
        inst.setCompletedAt(LocalDateTime.now());
        inst = instanceRepository.save(inst);
        assertEquals(1L, inst.getOptLockVersion());

        Optional<WorkflowInstance> found = instanceRepository.findById(inst.getId());
        assertTrue(found.isPresent());
        assertEquals(WorkflowStatus.COMPLETED, found.get().getStatus());
        assertEquals(1L, found.get().getOptLockVersion());
    }

    @Test
    void findById_shouldReturnInstance() {
        WorkflowInstance inst = instanceRepository.save(createInstance(WorkflowStatus.RUNNING, "corr-find"));
        Optional<WorkflowInstance> found = instanceRepository.findById(inst.getId());
        assertTrue(found.isPresent());
        assertEquals("corr-find", found.get().getCorrelationId());
        assertEquals(WorkflowStatus.RUNNING, found.get().getStatus());
    }

    @Test
    void findById_shouldReturnEmptyForNonExistent() {
        assertTrue(instanceRepository.findById(9999L).isEmpty());
    }

    @Test
    void findByWorkflowDefinitionId_shouldReturnMatchingInstances() {
        instanceRepository.save(createInstance(WorkflowStatus.RUNNING, "corr-a"));
        instanceRepository.save(createInstance(WorkflowStatus.COMPLETED, "corr-b"));
        assertEquals(2, instanceRepository.findByWorkflowDefinitionId(workflowDefId).size());
    }

    @Test
    void findByStatus_shouldFilterByStatus() {
        instanceRepository.save(createInstance(WorkflowStatus.RUNNING, "corr-r1"));
        instanceRepository.save(createInstance(WorkflowStatus.RUNNING, "corr-r2"));
        instanceRepository.save(createInstance(WorkflowStatus.COMPLETED, "corr-c1"));

        assertEquals(2, instanceRepository.findByStatus(WorkflowStatus.RUNNING).size());
        assertEquals(1, instanceRepository.findByStatus(WorkflowStatus.COMPLETED).size());
        assertEquals(0, instanceRepository.findByStatus(WorkflowStatus.FAILED).size());
    }

    @Test
    void findByCorrelationId_shouldReturnMatchingInstance() {
        instanceRepository.save(createInstance(WorkflowStatus.RUNNING, "unique-corr"));
        Optional<WorkflowInstance> found = instanceRepository.findByCorrelationId("unique-corr");
        assertTrue(found.isPresent());
        assertEquals("unique-corr", found.get().getCorrelationId());
    }

    @Test
    void findByCorrelationId_shouldReturnEmptyForNonExistent() {
        assertTrue(instanceRepository.findByCorrelationId("nonexistent").isEmpty());
    }

    @Test
    void findByWorkflowDefinitionIdAndStatus_shouldFilterBoth() {
        instanceRepository.save(createInstance(WorkflowStatus.RUNNING, "corr-ds1"));
        instanceRepository.save(createInstance(WorkflowStatus.RUNNING, "corr-ds2"));
        instanceRepository.save(createInstance(WorkflowStatus.COMPLETED, "corr-ds3"));

        List<WorkflowInstance> results = instanceRepository
                .findByWorkflowDefinitionIdAndStatus(workflowDefId, WorkflowStatus.RUNNING);
        assertEquals(2, results.size());
    }

    @Test
    void save_shouldPreserveAllFields() {
        WorkflowInstance inst = createInstance(WorkflowStatus.RUNNING, "corr-fields");
        inst.setCurrentNodeKey("process");
        inst = instanceRepository.save(inst);

        Optional<WorkflowInstance> found = instanceRepository.findById(inst.getId());
        assertTrue(found.isPresent());
        WorkflowInstance loaded = found.get();
        assertEquals(workflowDefId, loaded.getWorkflowDefinitionId());
        assertEquals("corr-fields", loaded.getCorrelationId());
        assertEquals(WorkflowStatus.RUNNING, loaded.getStatus());
        assertEquals("process", loaded.getCurrentNodeKey());
        assertEquals("test-user", loaded.getCreatedBy());
        assertNotNull(loaded.getCreatedAt());
    }
}
