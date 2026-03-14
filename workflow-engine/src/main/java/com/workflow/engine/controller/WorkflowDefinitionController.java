package com.workflow.engine.controller;

import com.workflow.engine.dto.request.CreateWorkflowDefinitionRequest;
import com.workflow.engine.dto.response.ApiResponse;
import com.workflow.engine.dto.response.WorkflowDefinitionResponse;
import com.workflow.engine.service.WorkflowDefinitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow-definitions")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService definitionService;

    public WorkflowDefinitionController(WorkflowDefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> createDefinition(
            @Valid @RequestBody CreateWorkflowDefinitionRequest request) {
        WorkflowDefinitionResponse response = definitionService.createDefinition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workflow definition created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> getDefinition(@PathVariable Long id) {
        WorkflowDefinitionResponse response = definitionService.getDefinition(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowDefinitionResponse>>> getAllDefinitions() {
        List<WorkflowDefinitionResponse> responses = definitionService.getAllDefinitions();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<WorkflowDefinitionResponse>>> getActiveDefinitions() {
        List<WorkflowDefinitionResponse> responses = definitionService.getActiveDefinitions();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> activateDefinition(@PathVariable Long id) {
        WorkflowDefinitionResponse response = definitionService.activateDefinition(id);
        return ResponseEntity.ok(ApiResponse.success("Workflow definition activated", response));
    }

    @PutMapping("/{id}/deprecate")
    public ResponseEntity<ApiResponse<WorkflowDefinitionResponse>> deprecateDefinition(@PathVariable Long id) {
        WorkflowDefinitionResponse response = definitionService.deprecateDefinition(id);
        return ResponseEntity.ok(ApiResponse.success("Workflow definition deprecated", response));
    }
}
