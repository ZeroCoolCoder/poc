package com.workflow.engine.model.definition;

import java.util.Objects;

public class TransitionDefinition {

    private Long id;
    private String name;
    private String sourceNodeKey;
    private String targetNodeKey;
    private String conditionExpression;
    private Integer priority;
    private Long workflowDefinitionId;

    public TransitionDefinition() {
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

    public Long getWorkflowDefinitionId() {
        return workflowDefinitionId;
    }

    public void setWorkflowDefinitionId(Long workflowDefinitionId) {
        this.workflowDefinitionId = workflowDefinitionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransitionDefinition that = (TransitionDefinition) o;
        return Objects.equals(sourceNodeKey, that.sourceNodeKey) &&
               Objects.equals(targetNodeKey, that.targetNodeKey) &&
               Objects.equals(workflowDefinitionId, that.workflowDefinitionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceNodeKey, targetNodeKey);
    }
}
