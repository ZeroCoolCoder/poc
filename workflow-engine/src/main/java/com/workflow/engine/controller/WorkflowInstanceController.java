package com.workflow.engine.controller;

import com.workflow.engine.dto.request.StartWorkflowRequest;
import com.workflow.engine.dto.request.SubmitActionRequest;
import com.workflow.engine.dto.response.ApiResponse;
import com.workflow.engine.dto.response.NodeExecutionResponse;
import com.workflow.engine.dto.response.WorkflowInstanceResponse;
import com.workflow.engine.model.instance.WorkflowStatus;
import com.workflow.engine.service.WorkflowExecutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow-instances")
public class WorkflowInstanceController {

    private final WorkflowExecutionService executionService;

    public WorkflowInstanceController(WorkflowExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> startWorkflow(
            @Valid @RequestBody StartWorkflowRequest request) {
        WorkflowInstanceResponse response = executionService.startWorkflow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workflow instance started", response));
    }

    @PostMapping("/action")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> submitAction(
            @Valid @RequestBody SubmitActionRequest request) {
        WorkflowInstanceResponse response = executionService.submitAction(request);
        return ResponseEntity.ok(ApiResponse.success("Action submitted", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> getInstance(@PathVariable Long id) {
        WorkflowInstanceResponse response = executionService.getInstance(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-definition/{definitionId}")
    public ResponseEntity<ApiResponse<List<WorkflowInstanceResponse>>> getInstancesByDefinition(
            @PathVariable Long definitionId) {
        List<WorkflowInstanceResponse> responses = executionService.getInstancesByDefinition(definitionId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/by-status")
    public ResponseEntity<ApiResponse<List<WorkflowInstanceResponse>>> getInstancesByStatus(
            @RequestParam WorkflowStatus status) {
        List<WorkflowInstanceResponse> responses = executionService.getInstancesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<ApiResponse<List<NodeExecutionResponse>>> getNodeExecutions(@PathVariable Long id) {
        List<NodeExecutionResponse> responses = executionService.getNodeExecutions(id);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> cancelWorkflow(@PathVariable Long id) {
        WorkflowInstanceResponse response = executionService.cancelWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success("Workflow instance cancelled", response));
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> suspendWorkflow(@PathVariable Long id) {
        WorkflowInstanceResponse response = executionService.suspendWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success("Workflow instance suspended", response));
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> resumeWorkflow(@PathVariable Long id) {
        WorkflowInstanceResponse response = executionService.resumeWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success("Workflow instance resumed", response));
    }
}
