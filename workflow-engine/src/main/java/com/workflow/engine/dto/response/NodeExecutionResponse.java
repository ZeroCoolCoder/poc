package com.workflow.engine.dto.response;

import com.workflow.engine.model.instance.NodeExecutionStatus;

import java.time.LocalDateTime;

public class NodeExecutionResponse {

    private Long id;
    private Long workflowInstanceId;
    private Long nodeDefinitionId;
    private String nodeKey;
    private NodeExecutionStatus status;
    private String inputData;
    private String outputData;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

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
