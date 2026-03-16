package com.workflow.engine.model.definition;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class WorkflowDefinition {

    private Long id;
    private String name;
    private String description;
    private Integer version;
    private DefinitionStatus status;
    private Set<NodeDefinition> nodes = new HashSet<>();
    private Set<TransitionDefinition> transitions = new HashSet<>();
    private String rulesEngineType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkflowDefinition() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public DefinitionStatus getStatus() {
        return status;
    }

    public void setStatus(DefinitionStatus status) {
        this.status = status;
    }

    public Set<NodeDefinition> getNodes() {
        return nodes;
    }

    public void setNodes(Set<NodeDefinition> nodes) {
        this.nodes = nodes;
    }

    public Set<TransitionDefinition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Set<TransitionDefinition> transitions) {
        this.transitions = transitions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRulesEngineType() {
        return rulesEngineType;
    }

    public void setRulesEngineType(String rulesEngineType) {
        this.rulesEngineType = rulesEngineType;
    }

    public void addNode(NodeDefinition node) {
        nodes.add(node);
        node.setWorkflowDefinitionId(this.id);
    }

    public void addTransition(TransitionDefinition transition) {
        transitions.add(transition);
        transition.setWorkflowDefinitionId(this.id);
    }
}
