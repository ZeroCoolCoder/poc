package com.workflow.engine.model.instance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "WF_TRANSITION_LOG")
public class TransitionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_trans_log_seq")
    @SequenceGenerator(name = "wf_trans_log_seq", sequenceName = "WF_TRANS_LOG_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "WORKFLOW_INSTANCE_ID", nullable = false)
    private Long workflowInstanceId;

    @Column(name = "TRANSITION_DEFINITION_ID")
    private Long transitionDefinitionId;

    @Column(name = "FROM_NODE_KEY", nullable = false, length = 100)
    private String fromNodeKey;

    @Column(name = "TO_NODE_KEY", nullable = false, length = 100)
    private String toNodeKey;

    @Column(name = "CONDITION_RESULT", length = 500)
    private String conditionResult;

    @Column(name = "TRANSITIONED_AT", nullable = false)
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
