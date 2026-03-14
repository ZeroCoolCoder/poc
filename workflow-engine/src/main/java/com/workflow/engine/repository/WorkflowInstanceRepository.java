package com.workflow.engine.repository;

import com.workflow.engine.model.instance.WorkflowInstance;
import com.workflow.engine.model.instance.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

    List<WorkflowInstance> findByWorkflowDefinitionId(Long workflowDefinitionId);

    List<WorkflowInstance> findByStatus(WorkflowStatus status);

    Optional<WorkflowInstance> findByCorrelationId(String correlationId);

    List<WorkflowInstance> findByWorkflowDefinitionIdAndStatus(Long workflowDefinitionId, WorkflowStatus status);
}
