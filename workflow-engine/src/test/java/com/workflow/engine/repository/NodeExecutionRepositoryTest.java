package com.workflow.engine.repository;

import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.WorkflowDefinition;
import com.workflow.engine.model.instance.NodeExecution;
import com.workflow.engine.model.instance.NodeExecutionStatus;
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
@Import({NodeExecutionRepository.class, WorkflowInstanceRepository.class,
         WorkflowDefinitionRepository.class, NodeDefinitionRepository.class,
         TransitionDefinitionRepository.class})
class NodeExecutionRepositoryTest {

    @Autowired
    private NodeExecutionRepository execRepository;

    @Autowired
    private WorkflowInstanceRepository instanceRepository;

    @Autowired
    private WorkflowDefinitionRepository defRepository;

    private Long instanceId;
    private Long nodeDefId;

    @BeforeEach
    void setUp() {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setName("exec-test-wf");
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
        nodeDefId = def.getNodes().iterator().next().getId();

        WorkflowInstance inst = new WorkflowInstance();
        inst.setWorkflowDefinitionId(def.getId());
        inst.setStatus(WorkflowStatus.RUNNING);
        inst.setCurrentNodeKey("start");
        inst.setCreatedAt(LocalDateTime.now());
        inst = instanceRepository.save(inst);
        instanceId = inst.getId();
    }

    private NodeExecution createExecution(String nodeKey, NodeExecutionStatus status) {
        NodeExecution exec = new NodeExecution();
        exec.setWorkflowInstanceId(instanceId);
        exec.setNodeDefinitionId(nodeDefId);
        exec.setNodeKey(nodeKey);
        exec.setStatus(status);
        exec.setRetryCount(0);
        exec.setStartedAt(LocalDateTime.now());
        exec.setInputData("{\"input\": \"data\"}");
        return exec;
    }

    @Test
    void save_shouldInsertNewExecutionAndAssignId() {
        NodeExecution exec = createExecution("start", NodeExecutionStatus.PENDING);
        exec = execRepository.save(exec);
        assertNotNull(exec.getId());
    }

    @Test
    void save_shouldUpdateExistingExecution() {
        NodeExecution exec = execRepository.save(createExecution("start", NodeExecutionStatus.PENDING));
        exec.setStatus(NodeExecutionStatus.COMPLETED);
        exec.setOutputData("{\"result\": \"ok\"}");
        exec.setCompletedAt(LocalDateTime.now());
        exec = execRepository.save(exec);

        List<NodeExecution> found = execRepository.findByWorkflowInstanceIdAndNodeKey(instanceId, "start");
        assertEquals(1, found.size());
        assertEquals(NodeExecutionStatus.COMPLETED, found.get(0).getStatus());
    }

    @Test
    void findByWorkflowInstanceId_shouldReturnAllExecutions() {
        execRepository.save(createExecution("start", NodeExecutionStatus.COMPLETED));
        execRepository.save(createExecution("process", NodeExecutionStatus.RUNNING));
        execRepository.save(createExecution("end", NodeExecutionStatus.PENDING));

        assertEquals(3, execRepository.findByWorkflowInstanceId(instanceId).size());
    }

    @Test
    void findByWorkflowInstanceId_shouldReturnEmptyForNonExistent() {
        assertTrue(execRepository.findByWorkflowInstanceId(9999L).isEmpty());
    }

    @Test
    void findByWorkflowInstanceIdAndStatus_shouldFilterByStatus() {
        execRepository.save(createExecution("start", NodeExecutionStatus.COMPLETED));
        execRepository.save(createExecution("process", NodeExecutionStatus.WAITING_FOR_INPUT));
        execRepository.save(createExecution("approve", NodeExecutionStatus.WAITING_FOR_INPUT));

        assertEquals(2, execRepository.findByWorkflowInstanceIdAndStatus(instanceId, NodeExecutionStatus.WAITING_FOR_INPUT).size());
        assertEquals(1, execRepository.findByWorkflowInstanceIdAndStatus(instanceId, NodeExecutionStatus.COMPLETED).size());
        assertEquals(0, execRepository.findByWorkflowInstanceIdAndStatus(instanceId, NodeExecutionStatus.FAILED).size());
    }

    @Test
    void findByWorkflowInstanceIdAndNodeKey_shouldReturnMatchingExecutions() {
        execRepository.save(createExecution("start", NodeExecutionStatus.COMPLETED));
        execRepository.save(createExecution("process", NodeExecutionStatus.RUNNING));

        List<NodeExecution> results = execRepository.findByWorkflowInstanceIdAndNodeKey(instanceId, "start");
        assertEquals(1, results.size());
        assertEquals("start", results.get(0).getNodeKey());
    }

    @Test
    void findByWorkflowInstanceIdAndNodeKey_shouldReturnEmptyForNonExistent() {
        assertTrue(execRepository.findByWorkflowInstanceIdAndNodeKey(instanceId, "nonexistent").isEmpty());
    }

    @Test
    void findByWorkflowInstanceIdOrderByStartedAtAsc_shouldReturnOrdered() {
        NodeExecution exec1 = createExecution("start", NodeExecutionStatus.COMPLETED);
        exec1.setStartedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        execRepository.save(exec1);

        NodeExecution exec2 = createExecution("process", NodeExecutionStatus.COMPLETED);
        exec2.setStartedAt(LocalDateTime.of(2026, 1, 1, 11, 0));
        execRepository.save(exec2);

        NodeExecution exec3 = createExecution("end", NodeExecutionStatus.COMPLETED);
        exec3.setStartedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        execRepository.save(exec3);

        List<NodeExecution> results = execRepository.findByWorkflowInstanceIdOrderByStartedAtAsc(instanceId);
        assertEquals(3, results.size());
        assertEquals("start", results.get(0).getNodeKey());
        assertEquals("process", results.get(1).getNodeKey());
        assertEquals("end", results.get(2).getNodeKey());
    }

    @Test
    void save_shouldHandleErrorMessage() {
        NodeExecution exec = execRepository.save(createExecution("failing", NodeExecutionStatus.RUNNING));
        exec.setStatus(NodeExecutionStatus.FAILED);
        exec.setErrorMessage("Something went wrong");
        exec.setRetryCount(3);
        exec.setCompletedAt(LocalDateTime.now());
        execRepository.save(exec);

        List<NodeExecution> found = execRepository.findByWorkflowInstanceIdAndNodeKey(instanceId, "failing");
        assertEquals(1, found.size());
        assertEquals(NodeExecutionStatus.FAILED, found.get(0).getStatus());
        assertEquals("Something went wrong", found.get(0).getErrorMessage());
        assertEquals(3, found.get(0).getRetryCount());
    }
}
