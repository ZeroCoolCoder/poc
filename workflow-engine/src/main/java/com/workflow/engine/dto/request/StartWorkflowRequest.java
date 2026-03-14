package com.workflow.engine.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class StartWorkflowRequest {

    @NotNull(message = "Workflow definition ID is required")
    private Long workflowDefinitionId;

    private String correlationId;

    private Map<String, Object> initialContext;

    private String createdBy;

    public Long getWorkflowDefinitionId() {
        return workflowDefinitionId;
    }

    public void setWorkflowDefinitionId(Long workflowDefinitionId) {
        this.workflowDefinitionId = workflowDefinitionId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Map<String, Object> getInitialContext() {
        return initialContext;
    }

    public void setInitialContext(Map<String, Object> initialContext) {
        this.initialContext = initialContext;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
