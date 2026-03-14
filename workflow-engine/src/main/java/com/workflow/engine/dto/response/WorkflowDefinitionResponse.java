package com.workflow.engine.dto.response;

import com.workflow.engine.model.definition.DefinitionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class WorkflowDefinitionResponse {

    private Long id;
    private String name;
    private String description;
    private Integer version;
    private DefinitionStatus status;
    private List<NodeDefinitionResponse> nodes;
    private List<TransitionDefinitionResponse> transitions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public List<NodeDefinitionResponse> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeDefinitionResponse> nodes) {
        this.nodes = nodes;
    }

    public List<TransitionDefinitionResponse> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<TransitionDefinitionResponse> transitions) {
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
}
