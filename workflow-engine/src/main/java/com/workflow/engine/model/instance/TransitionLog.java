package com.workflow.engine.model.instance;

import java.time.LocalDateTime;

public class TransitionLog {

    private Long id;
    private Long workflowInstanceId;
    private Long transitionDefinitionId;
    private String fromNodeKey;
    private String toNodeKey;
    private String conditionResult;
    private LocalDateTime transitionedAt;

    public TransitionLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(Long workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public Long getTransitionDefinitionId() {
        return transitionDefinitionId;
    }

    public void setTransitionDefinitionId(Long transitionDefinitionId) {
        this.transitionDefinitionId = transitionDefinitionId;
    }

    public String getFromNodeKey() {
        return fromNodeKey;
    }

    public void setFromNodeKey(String fromNodeKey) {
        this.fromNodeKey = fromNodeKey;
    }

    public String getToNodeKey() {
        return toNodeKey;
    }

    public void setToNodeKey(String toNodeKey) {
        this.toNodeKey = toNodeKey;
    }

    public String getConditionResult() {
        return conditionResult;
    }

    public void setConditionResult(String conditionResult) {
        this.conditionResult = conditionResult;
    }

    public LocalDateTime getTransitionedAt() {
        return transitionedAt;
    }

    public void setTransitionedAt(LocalDateTime transitionedAt) {
        this.transitionedAt = transitionedAt;
    }
}
