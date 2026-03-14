package com.workflow.engine.model.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "WF_TRANSITION_DEFINITION")
public class TransitionDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_trans_def_seq")
    @SequenceGenerator(name = "wf_trans_def_seq", sequenceName = "WF_TRANS_DEF_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "SOURCE_NODE_KEY", nullable = false, length = 100)
    private String sourceNodeKey;

    @Column(name = "TARGET_NODE_KEY", nullable = false, length = 100)
    private String targetNodeKey;

    @Column(name = "CONDITION_EXPRESSION", length = 2000)
    private String conditionExpression;

    @Column(name = "PRIORITY")
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_DEFINITION_ID", nullable = false)
    @JsonIgnore
    private WorkflowDefinition workflowDefinition;

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

    public WorkflowDefinition getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }
}
