package com.workflow.engine.repository;

import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    List<WorkflowDefinition> findByStatus(DefinitionStatus status);

    List<WorkflowDefinition> findByName(String name);

    @Query("SELECT wd FROM WorkflowDefinition wd WHERE wd.name = :name AND wd.version = :version")
    Optional<WorkflowDefinition> findByNameAndVersion(@Param("name") String name, @Param("version") Integer version);

    @Query("SELECT wd FROM WorkflowDefinition wd WHERE wd.name = :name ORDER BY wd.version DESC")
    List<WorkflowDefinition> findByNameOrderByVersionDesc(@Param("name") String name);

    @Query("SELECT wd FROM WorkflowDefinition wd LEFT JOIN FETCH wd.nodes LEFT JOIN FETCH wd.transitions WHERE wd.id = :id")
    Optional<WorkflowDefinition> findByIdWithNodesAndTransitions(@Param("id") Long id);
}
