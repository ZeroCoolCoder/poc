package com.workflow.engine.service;

import com.workflow.engine.dto.request.CreateWorkflowDefinitionRequest;
import com.workflow.engine.dto.request.NodeDefinitionRequest;
import com.workflow.engine.dto.request.TransitionDefinitionRequest;
import com.workflow.engine.dto.response.NodeDefinitionResponse;
import com.workflow.engine.dto.response.TransitionDefinitionResponse;
import com.workflow.engine.dto.response.WorkflowDefinitionResponse;
import com.workflow.engine.exception.WorkflowException;
import com.workflow.engine.model.definition.DefinitionStatus;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.TransitionDefinition;
import com.workflow.engine.model.definition.WorkflowDefinition;
import com.workflow.engine.repository.WorkflowDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkflowDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowDefinitionService.class);

    private final WorkflowDefinitionRepository definitionRepository;

    public WorkflowDefinitionService(WorkflowDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    @Transactional
    public WorkflowDefinitionResponse createDefinition(CreateWorkflowDefinitionRequest request) {
        validateDefinition(request);

        List<WorkflowDefinition> existing = definitionRepository.findByNameOrderByVersionDesc(request.getName());
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName(request.getName());
        definition.setDescription(request.getDescription());
        definition.setVersion(nextVersion);
        definition.setStatus(DefinitionStatus.DRAFT);
        definition.setCreatedAt(LocalDateTime.now());
        definition.setUpdatedAt(LocalDateTime.now());

        for (NodeDefinitionRequest nodeReq : request.getNodes()) {
            NodeDefinition node = new NodeDefinition();
            node.setNodeKey(nodeReq.getNodeKey());
            node.setName(nodeReq.getName());
            node.setDescription(nodeReq.getDescription());
            node.setNodeType(nodeReq.getNodeType());
            node.setActionHandler(nodeReq.getActionHandler());
            node.setConfiguration(nodeReq.getConfiguration());
            node.setTimeoutSeconds(nodeReq.getTimeoutSeconds());
            node.setMaxRetries(nodeReq.getMaxRetries());
            definition.addNode(node);
        }

        if (request.getTransitions() != null) {
            for (TransitionDefinitionRequest transReq : request.getTransitions()) {
                TransitionDefinition transition = new TransitionDefinition();
                transition.setName(transReq.getName());
                transition.setSourceNodeKey(transReq.getSourceNodeKey());
                transition.setTargetNodeKey(transReq.getTargetNodeKey());
                transition.setConditionExpression(transReq.getConditionExpression());
                transition.setPriority(transReq.getPriority() != null ? transReq.getPriority() : 0);
                definition.addTransition(transition);
            }
        }

        definition = definitionRepository.save(definition);
        log.info("Created workflow definition '{}' version {}", definition.getName(), definition.getVersion());

        return toResponse(definition);
    }

    @Transactional
    public WorkflowDefinitionResponse activateDefinition(Long id) {
        WorkflowDefinition definition = definitionRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Workflow definition not found: " + id));

        definition.setStatus(DefinitionStatus.ACTIVE);
        definition.setUpdatedAt(LocalDateTime.now());
        definition = definitionRepository.save(definition);

        log.info("Activated workflow definition '{}' version {}", definition.getName(), definition.getVersion());
        return toResponse(definition);
    }

    @Transactional
    public WorkflowDefinitionResponse deprecateDefinition(Long id) {
        WorkflowDefinition definition = definitionRepository.findById(id)
                .orElseThrow(() -> new WorkflowException("Workflow definition not found: " + id));

        definition.setStatus(DefinitionStatus.DEPRECATED);
        definition.setUpdatedAt(LocalDateTime.now());
        definition = definitionRepository.save(definition);

        log.info("Deprecated workflow definition '{}' version {}", definition.getName(), definition.getVersion());
        return toResponse(definition);
    }

    @Transactional(readOnly = true)
    public WorkflowDefinitionResponse getDefinition(Long id) {
        WorkflowDefinition definition = definitionRepository.findByIdWithNodesAndTransitions(id)
                .orElseThrow(() -> new WorkflowException("Workflow definition not found: " + id));
        return toResponse(definition);
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinitionResponse> getAllDefinitions() {
        return definitionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinitionResponse> getActiveDefinitions() {
        return definitionRepository.findByStatus(DefinitionStatus.ACTIVE).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkflowDefinition getDefinitionEntity(Long id) {
        return definitionRepository.findByIdWithNodesAndTransitions(id)
                .orElseThrow(() -> new WorkflowException("Workflow definition not found: " + id));
    }

    private void validateDefinition(CreateWorkflowDefinitionRequest request) {
        long startCount = request.getNodes().stream()
                .filter(n -> n.getNodeType() == NodeType.START).count();
        if (startCount != 1) {
            throw new WorkflowException("Workflow must have exactly one START node, found: " + startCount);
        }

        long endCount = request.getNodes().stream()
                .filter(n -> n.getNodeType() == NodeType.END).count();
        if (endCount < 1) {
            throw new WorkflowException("Workflow must have at least one END node");
        }

        Set<String> nodeKeys = new HashSet<>();
        for (NodeDefinitionRequest node : request.getNodes()) {
            if (!nodeKeys.add(node.getNodeKey())) {
                throw new WorkflowException("Duplicate node key: " + node.getNodeKey());
            }
        }

        if (request.getTransitions() != null) {
            for (TransitionDefinitionRequest trans : request.getTransitions()) {
                if (!nodeKeys.contains(trans.getSourceNodeKey())) {
                    throw new WorkflowException("Transition source node not found: " + trans.getSourceNodeKey());
                }
                if (!nodeKeys.contains(trans.getTargetNodeKey())) {
                    throw new WorkflowException("Transition target node not found: " + trans.getTargetNodeKey());
                }
            }
        }
    }

    private WorkflowDefinitionResponse toResponse(WorkflowDefinition definition) {
        WorkflowDefinitionResponse response = new WorkflowDefinitionResponse();
        response.setId(definition.getId());
        response.setName(definition.getName());
        response.setDescription(definition.getDescription());
        response.setVersion(definition.getVersion());
        response.setStatus(definition.getStatus());
        response.setCreatedAt(definition.getCreatedAt());
        response.setUpdatedAt(definition.getUpdatedAt());

        if (definition.getNodes() != null) {
            response.setNodes(definition.getNodes().stream()
                    .map(this::toNodeResponse)
                    .collect(Collectors.toList()));
        }

        if (definition.getTransitions() != null) {
            response.setTransitions(definition.getTransitions().stream()
                    .map(this::toTransitionResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private NodeDefinitionResponse toNodeResponse(NodeDefinition node) {
        NodeDefinitionResponse response = new NodeDefinitionResponse();
        response.setId(node.getId());
        response.setNodeKey(node.getNodeKey());
        response.setName(node.getName());
        response.setDescription(node.getDescription());
        response.setNodeType(node.getNodeType());
        response.setActionHandler(node.getActionHandler());
        response.setConfiguration(node.getConfiguration());
        response.setTimeoutSeconds(node.getTimeoutSeconds());
        response.setMaxRetries(node.getMaxRetries());
        return response;
    }

    private TransitionDefinitionResponse toTransitionResponse(TransitionDefinition transition) {
        TransitionDefinitionResponse response = new TransitionDefinitionResponse();
        response.setId(transition.getId());
        response.setName(transition.getName());
        response.setSourceNodeKey(transition.getSourceNodeKey());
        response.setTargetNodeKey(transition.getTargetNodeKey());
        response.setConditionExpression(transition.getConditionExpression());
        response.setPriority(transition.getPriority());
        return response;
    }
}
