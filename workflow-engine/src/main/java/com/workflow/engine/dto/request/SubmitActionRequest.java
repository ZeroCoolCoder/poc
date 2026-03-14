package com.workflow.engine.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class SubmitActionRequest {

    @NotNull(message = "Workflow instance ID is required")
    private Long workflowInstanceId;

    private String action;

    private Map<String, Object> payload;

    private String submittedBy;

    public Long getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(Long workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }
}
