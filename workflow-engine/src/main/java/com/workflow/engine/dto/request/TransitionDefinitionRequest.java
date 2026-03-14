package com.workflow.engine.dto.request;

import jakarta.validation.constraints.NotBlank;

public class TransitionDefinitionRequest {

    private String name;

    @NotBlank(message = "Source node key is required")
    private String sourceNodeKey;

    @NotBlank(message = "Target node key is required")
    private String targetNodeKey;

    private String conditionExpression;

    private Integer priority;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceNodeKey() {
        return sourceNodeKey;
    }

    public void setSourceNodeKey(String sourceNodeKey) {
        this.sourceNodeKey = sourceNodeKey;
    }

    public String getTargetNodeKey() {
        return targetNodeKey;
    }

    public void setTargetNodeKey(String targetNodeKey) {
        this.targetNodeKey = targetNodeKey;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
