package com.workflow.engine.model.instance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "WF_NODE_EXECUTION")
public class NodeExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_node_exec_seq")
    @SequenceGenerator(name = "wf_node_exec_seq", sequenceName = "WF_NODE_EXEC_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "WORKFLOW_INSTANCE_ID", nullable = false)
    private Long workflowInstanceId;

    @Column(name = "NODE_DEFINITION_ID", nullable = false)
    private Long nodeDefinitionId;

    @Column(name = "NODE_KEY", nullable = false, length = 100)
    private String nodeKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private NodeExecutionStatus status;

    @Column(name = "INPUT_DATA", length = 4000)
    private String inputData;

    @Column(name = "OUTPUT_DATA", length = 4000)
    private String outputData;

    @Column(name = "ERROR_MESSAGE", length = 2000)
    private String errorMessage;

    @Column(name = "RETRY_COUNT")
    private Integer retryCount;

    @Column(name = "STARTED_AT")
    private LocalDateTime startedAt;

    @Column(name = "COMPLETED_AT")
    private LocalDateTime completedAt;

    public NodeExecution() {
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

    public Long getNodeDefinitionId() {
        return nodeDefinitionId;
    }

    public void setNodeDefinitionId(Long nodeDefinitionId) {
        this.nodeDefinitionId = nodeDefinitionId;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public NodeExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(NodeExecutionStatus status) {
        this.status = status;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getOutputData() {
        return outputData;
    }

    public void setOutputData(String outputData) {
        this.outputData = outputData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
