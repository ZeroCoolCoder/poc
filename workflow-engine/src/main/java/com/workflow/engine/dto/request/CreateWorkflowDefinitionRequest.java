package com.workflow.engine.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreateWorkflowDefinitionRequest {

    @NotBlank(message = "Workflow name is required")
    private String name;

    private String description;

    @NotEmpty(message = "At least one node is required")
    @Valid
    private List<NodeDefinitionRequest> nodes;

    @Valid
    private List<TransitionDefinitionRequest> transitions;

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

    public List<NodeDefinitionRequest> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeDefinitionRequest> nodes) {
        this.nodes = nodes;
    }

    public List<TransitionDefinitionRequest> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<TransitionDefinitionRequest> transitions) {
        this.transitions = transitions;
    }
}
