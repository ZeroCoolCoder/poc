package com.workflow.engine.repository;

import com.workflow.engine.model.instance.NodeExecution;
import com.workflow.engine.model.instance.NodeExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeExecutionRepository extends JpaRepository<NodeExecution, Long> {

    List<NodeExecution> findByWorkflowInstanceId(Long workflowInstanceId);

    List<NodeExecution> findByWorkflowInstanceIdAndStatus(Long workflowInstanceId, NodeExecutionStatus status);

    List<NodeExecution> findByWorkflowInstanceIdAndNodeKey(Long workflowInstanceId, String nodeKey);

    List<NodeExecution> findByWorkflowInstanceIdOrderByStartedAtAsc(Long workflowInstanceId);
}
