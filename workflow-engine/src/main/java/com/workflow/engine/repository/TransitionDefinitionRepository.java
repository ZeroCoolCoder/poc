package com.workflow.engine.repository;

import com.workflow.engine.model.definition.TransitionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransitionDefinitionRepository extends JpaRepository<TransitionDefinition, Long> {

    List<TransitionDefinition> findByWorkflowDefinitionId(Long workflowDefinitionId);

    List<TransitionDefinition> findByWorkflowDefinitionIdAndSourceNodeKeyOrderByPriorityAsc(
            Long workflowDefinitionId, String sourceNodeKey);
}
