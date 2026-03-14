package com.workflow.engine.repository;

import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeDefinitionRepository extends JpaRepository<NodeDefinition, Long> {

    List<NodeDefinition> findByWorkflowDefinitionId(Long workflowDefinitionId);

    Optional<NodeDefinition> findByWorkflowDefinitionIdAndNodeKey(Long workflowDefinitionId, String nodeKey);

    List<NodeDefinition> findByWorkflowDefinitionIdAndNodeType(Long workflowDefinitionId, NodeType nodeType);
}
