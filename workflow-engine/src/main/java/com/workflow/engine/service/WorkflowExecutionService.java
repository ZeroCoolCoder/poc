package com.workflow.engine.service;

import com.workflow.engine.cache.WorkflowCacheService;
import com.workflow.engine.dto.request.StartWorkflowRequest;
import com.workflow.engine.dto.request.SubmitActionRequest;
import com.workflow.engine.dto.response.NodeExecutionResponse;
import com.workflow.engine.dto.response.WorkflowInstanceResponse;
import com.workflow.engine.engine.WorkflowEngine;
import com.workflow.engine.exception.WorkflowException;
import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.WorkflowDefinition;
import com.workflow.engine.model.instance.NodeExecution;
import com.workflow.engine.model.instance.WorkflowInstance;
import com.workflow.engine.model.instance.WorkflowStatus;
import com.workflow.engine.repository.NodeExecutionRepository;
import com.workflow.engine.repository.WorkflowInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkflowExecutionService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutionService.class);

    private final WorkflowEngine workflowEngine;
    private final WorkflowDefinitionService definitionService;
    private final WorkflowInstanceRepository instanceRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final WorkflowCacheService cacheService;

    public WorkflowExecutionService(WorkflowEngine workflowEngine,
                                     WorkflowDefinitionService definitionService,
                                     WorkflowInstanceRepository instanceRepository,
                                     NodeExecutionRepository nodeExecutionRepository,
                                     WorkflowCacheService cacheService) {
        this.workflowEngine = workflowEngine;
        this.definitionService = definitionService;
        this.instanceRepository = instanceRepository;
        this.nodeExecutionRepository = nodeExecutionRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public WorkflowInstanceResponse startWorkflow(StartWorkflowRequest request) {
        WorkflowDefinition definition = definitionService.getDefinitionEntity(request.getWorkflowDefinitionId());

        if (definition.getStatus() != DefinitionStatus.ACTIVE) {
            throw new WorkflowException(
                    "Workflow definition '" + definition.getName() + "' is not ACTIVE (status: " + definition.getStatus() + ")");
        }

        WorkflowInstance instance = workflowEngine.startWorkflow(
                definition,
                request.getInitialContext(),
                request.getCorrelationId(),
                request.getCreatedBy());

        return toResponse(instance);
    }

    @Transactional
    public WorkflowInstanceResponse submitAction(SubmitActionRequest request) {
        WorkflowInstance instance = findInstance(request.getWorkflowInstanceId());
        WorkflowDefinition definition = definitionService.getDefinitionEntity(instance.getWorkflowDefinitionId());

        instance = workflowEngine.submitExternalAction(instance, definition, request.getNodeKey(), request.getAction(), request.getPayload());
        return toResponse(instance);
    }

    @Transactional(readOnly = true)
    public WorkflowInstanceResponse getInstance(Long instanceId) {
        Optional<WorkflowInstance> cached = cacheService.getCachedInstance(instanceId);
        if (cached.isPresent()) {
            return toResponse(cached.get());
        }

        WorkflowInstance instance = findInstance(instanceId);
        return toResponse(instance);
    }

    @Transactional(readOnly = true)
    public List<WorkflowInstanceResponse> getInstancesByDefinition(Long definitionId) {
        return instanceRepository.findByWorkflowDefinitionId(definitionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowInstanceResponse> getInstancesByStatus(WorkflowStatus status) {
        return instanceRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NodeExecutionResponse> getNodeExecutions(Long instanceId) {
        return nodeExecutionRepository.findByWorkflowInstanceIdOrderByStartedAtAsc(instanceId).stream()
                .map(this::toNodeExecutionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkflowInstanceResponse cancelWorkflow(Long instanceId) {
        WorkflowInstance instance = findInstance(instanceId);

        if (instance.getStatus() == WorkflowStatus.COMPLETED || instance.getStatus() == WorkflowStatus.CANCELLED) {
            throw new WorkflowException("Cannot cancel workflow instance " + instanceId + " in state: " + instance.getStatus());
        }

        instance.setStatus(WorkflowStatus.CANCELLED);
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setCompletedAt(LocalDateTime.now());
        instance = instanceRepository.save(instance);

        cacheService.cacheWorkflowInstance(instance);
        log.info("Cancelled workflow instance {}", instanceId);

        return toResponse(instance);
    }

    @Transactional
    public WorkflowInstanceResponse suspendWorkflow(Long instanceId) {
        WorkflowInstance instance = findInstance(instanceId);

        if (instance.getStatus() != WorkflowStatus.RUNNING) {
            throw new WorkflowException("Can only suspend RUNNING workflow instances");
        }

        instance.setStatus(WorkflowStatus.SUSPENDED);
        instance.setUpdatedAt(LocalDateTime.now());
        instance = instanceRepository.save(instance);

        cacheService.cacheWorkflowInstance(instance);
        log.info("Suspended workflow instance {}", instanceId);

        return toResponse(instance);
    }

    @Transactional
    public WorkflowInstanceResponse resumeWorkflow(Long instanceId) {
        WorkflowInstance instance = findInstance(instanceId);

        if (instance.getStatus() != WorkflowStatus.SUSPENDED) {
            throw new WorkflowException("Can only resume SUSPENDED workflow instances");
        }

        instance.setStatus(WorkflowStatus.RUNNING);
        instance.setUpdatedAt(LocalDateTime.now());
        instance = instanceRepository.save(instance);

        cacheService.cacheWorkflowInstance(instance);
        log.info("Resumed workflow instance {}", instanceId);

        return toResponse(instance);
    }

    private WorkflowInstance findInstance(Long instanceId) {
        return instanceRepository.findById(instanceId)
                .orElseThrow(() -> new WorkflowException("Workflow instance not found: " + instanceId));
    }

    private WorkflowInstanceResponse toResponse(WorkflowInstance instance) {
        WorkflowInstanceResponse response = new WorkflowInstanceResponse();
        response.setId(instance.getId());
        response.setWorkflowDefinitionId(instance.getWorkflowDefinitionId());
        response.setCorrelationId(instance.getCorrelationId());
        response.setStatus(instance.getStatus());
        response.setCurrentNodeKey(instance.getCurrentNodeKey());
        response.setContextData(instance.getContextData());
        response.setCreatedAt(instance.getCreatedAt());
        response.setUpdatedAt(instance.getUpdatedAt());
        response.setCompletedAt(instance.getCompletedAt());
        response.setCreatedBy(instance.getCreatedBy());
        return response;
    }

    private NodeExecutionResponse toNodeExecutionResponse(NodeExecution execution) {
        NodeExecutionResponse response = new NodeExecutionResponse();
        response.setId(execution.getId());
        response.setWorkflowInstanceId(execution.getWorkflowInstanceId());
        response.setNodeDefinitionId(execution.getNodeDefinitionId());
        response.setNodeKey(execution.getNodeKey());
        response.setStatus(execution.getStatus());
        response.setInputData(execution.getInputData());
        response.setOutputData(execution.getOutputData());
        response.setErrorMessage(execution.getErrorMessage());
        response.setRetryCount(execution.getRetryCount());
        response.setStartedAt(execution.getStartedAt());
        response.setCompletedAt(execution.getCompletedAt());
        return response;
    }
}
