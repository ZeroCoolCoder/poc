package com.workflow.engine.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.engine.cache.WorkflowCacheService;
import com.workflow.engine.exception.WorkflowException;
import com.workflow.engine.model.definition.NodeDefinition;
import com.workflow.engine.model.definition.NodeType;
import com.workflow.engine.model.definition.TransitionDefinition;
import com.workflow.engine.model.definition.WorkflowDefinition;
import com.workflow.engine.model.instance.NodeExecution;
import com.workflow.engine.model.instance.NodeExecutionStatus;
import com.workflow.engine.model.instance.TransitionLog;
import com.workflow.engine.model.instance.WorkflowInstance;
import com.workflow.engine.model.instance.WorkflowStatus;
import com.workflow.engine.repository.NodeExecutionRepository;
import com.workflow.engine.repository.TransitionDefinitionRepository;
import com.workflow.engine.repository.TransitionLogRepository;
import com.workflow.engine.repository.WorkflowInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core workflow execution engine. Drives workflow instances through their
 * graph-based state machine, executing action handlers at AUTO nodes and
 * pausing at WAIT_FOR_INPUT nodes until external input is received.
 */
@Component
public class WorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

    private final ActionHandlerRegistry handlerRegistry;
    private final TransitionEvaluator transitionEvaluator;
    private final WorkflowInstanceRepository instanceRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final TransitionDefinitionRepository transitionDefinitionRepository;
    private final TransitionLogRepository transitionLogRepository;
    private final WorkflowCacheService cacheService;
    private final ObjectMapper objectMapper;

    public WorkflowEngine(ActionHandlerRegistry handlerRegistry,
                          TransitionEvaluator transitionEvaluator,
                          WorkflowInstanceRepository instanceRepository,
                          NodeExecutionRepository nodeExecutionRepository,
                          TransitionDefinitionRepository transitionDefinitionRepository,
                          TransitionLogRepository transitionLogRepository,
                          WorkflowCacheService cacheService,
                          ObjectMapper objectMapper) {
        this.handlerRegistry = handlerRegistry;
        this.transitionEvaluator = transitionEvaluator;
        this.instanceRepository = instanceRepository;
        this.nodeExecutionRepository = nodeExecutionRepository;
        this.transitionDefinitionRepository = transitionDefinitionRepository;
        this.transitionLogRepository = transitionLogRepository;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }

    /**
     * Starts a new workflow instance from the START node and begins execution.
     */
    @Transactional
    public WorkflowInstance startWorkflow(WorkflowDefinition definition,
                                          Map<String, Object> initialContext,
                                          String correlationId,
                                          String createdBy) {
        NodeDefinition startNode = definition.getNodes().stream()
                .filter(n -> n.getNodeType() == NodeType.START)
                .findFirst()
                .orElseThrow(() -> new WorkflowException("No START node found in workflow definition: " + definition.getName()));

        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowDefinitionId(definition.getId());
        instance.setCorrelationId(correlationId);
        instance.setStatus(WorkflowStatus.RUNNING);
        instance.setCurrentNodeKey(startNode.getNodeKey());
        instance.setContextData(serializeContext(initialContext != null ? initialContext : new HashMap<>()));
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        instance.setCreatedBy(createdBy);

        instance = instanceRepository.save(instance);

        cacheService.cacheWorkflowInstance(instance);

        log.info("Started workflow instance {} for definition '{}' (id={})",
                instance.getId(), definition.getName(), definition.getId());

        executeNode(instance, definition, startNode);

        return instanceRepository.findById(instance.getId()).orElse(instance);
    }

    /**
     * Resumes a workflow instance that is waiting for external input.
     */
    @Transactional
    public WorkflowInstance submitExternalAction(WorkflowInstance instance,
                                                  WorkflowDefinition definition,
                                                  String action,
                                                  Map<String, Object> payload) {
        if (instance.getStatus() != WorkflowStatus.RUNNING) {
            throw new WorkflowException("Workflow instance " + instance.getId() + " is not in RUNNING state");
        }

        String currentNodeKey = instance.getCurrentNodeKey();
        NodeDefinition currentNode = findNodeByKey(definition, currentNodeKey);

        if (currentNode.getNodeType() != NodeType.WAIT_FOR_INPUT) {
            throw new WorkflowException("Current node '" + currentNodeKey + "' is not a WAIT_FOR_INPUT node");
        }

        Map<String, Object> context = deserializeContext(instance.getContextData());
        if (action != null) {
            context.put("_action", action);
        }
        if (payload != null) {
            context.putAll(payload);
        }

        instance.setContextData(serializeContext(context));
        instance.setUpdatedAt(LocalDateTime.now());
        instanceRepository.save(instance);

        // Complete the waiting node execution
        List<NodeExecution> waitingExecutions = nodeExecutionRepository
                .findByWorkflowInstanceIdAndStatus(instance.getId(), NodeExecutionStatus.WAITING_FOR_INPUT);
        for (NodeExecution exec : waitingExecutions) {
            if (exec.getNodeKey().equals(currentNodeKey)) {
                exec.setStatus(NodeExecutionStatus.COMPLETED);
                exec.setOutputData(serializeContext(payload != null ? payload : new HashMap<>()));
                exec.setCompletedAt(LocalDateTime.now());
                nodeExecutionRepository.save(exec);
            }
        }

        advanceToNextNode(instance, definition, currentNode, context);

        return instanceRepository.findById(instance.getId()).orElse(instance);
    }

    /**
     * Asynchronously advances the workflow from a given node.
     */
    @Async("workflowExecutor")
    public void executeNodeAsync(WorkflowInstance instance, WorkflowDefinition definition, NodeDefinition node) {
        executeNode(instance, definition, node);
    }

    /**
     * Executes a node and advances the workflow.
     */
    @Transactional
    public void executeNode(WorkflowInstance instance, WorkflowDefinition definition, NodeDefinition node) {
        log.debug("Executing node '{}' (type={}) for workflow instance {}",
                node.getNodeKey(), node.getNodeType(), instance.getId());

        Map<String, Object> context = deserializeContext(instance.getContextData());

        NodeExecution execution = createNodeExecution(instance, node);

        switch (node.getNodeType()) {
            case START:
                execution.setStatus(NodeExecutionStatus.COMPLETED);
                execution.setCompletedAt(LocalDateTime.now());
                nodeExecutionRepository.save(execution);
                advanceToNextNode(instance, definition, node, context);
                break;

            case AUTO:
                executeAutoNode(instance, definition, node, execution, context);
                break;

            case WAIT_FOR_INPUT:
                execution.setStatus(NodeExecutionStatus.WAITING_FOR_INPUT);
                nodeExecutionRepository.save(execution);
                instance.setCurrentNodeKey(node.getNodeKey());
                instance.setUpdatedAt(LocalDateTime.now());
                instanceRepository.save(instance);
                cacheService.cacheWorkflowInstance(instance);
                log.info("Workflow instance {} waiting for input at node '{}'",
                        instance.getId(), node.getNodeKey());
                break;

            case FORK:
                executeForkNode(instance, definition, node, execution, context);
                break;

            case JOIN:
                executeJoinNode(instance, definition, node, execution, context);
                break;

            case END:
                execution.setStatus(NodeExecutionStatus.COMPLETED);
                execution.setCompletedAt(LocalDateTime.now());
                nodeExecutionRepository.save(execution);
                completeWorkflow(instance);
                break;
        }
    }

    private void executeAutoNode(WorkflowInstance instance, WorkflowDefinition definition,
                                  NodeDefinition node, NodeExecution execution,
                                  Map<String, Object> context) {
        String handlerName = node.getActionHandler();
        if (handlerName == null || handlerName.isBlank()) {
            execution.setStatus(NodeExecutionStatus.COMPLETED);
            execution.setCompletedAt(LocalDateTime.now());
            nodeExecutionRepository.save(execution);
            advanceToNextNode(instance, definition, node, context);
            return;
        }

        Optional<ActionHandler> handlerOpt = handlerRegistry.getHandler(handlerName);
        if (handlerOpt.isEmpty()) {
            handleNodeFailure(instance, execution,
                    "No action handler found with name: " + handlerName);
            return;
        }

        ActionHandler handler = handlerOpt.get();
        Map<String, Object> nodeConfig = deserializeContext(node.getConfiguration());

        int maxRetries = node.getMaxRetries() != null ? node.getMaxRetries() : 0;
        int attempt = 0;
        ActionResult result = null;

        while (attempt <= maxRetries) {
            try {
                execution.setStatus(NodeExecutionStatus.RUNNING);
                execution.setRetryCount(attempt);
                nodeExecutionRepository.save(execution);

                result = handler.execute(context, nodeConfig, deserializeContext(execution.getInputData()));

                if (result.isSuccess()) {
                    break;
                }

                attempt++;
                if (attempt > maxRetries) {
                    break;
                }

                log.warn("Action handler '{}' failed at node '{}', retrying ({}/{}): {}",
                        handlerName, node.getNodeKey(), attempt, maxRetries, result.getErrorMessage());
            } catch (Exception e) {
                attempt++;
                if (attempt > maxRetries) {
                    handleNodeFailure(instance, execution,
                            "Exception in action handler '" + handlerName + "': " + e.getMessage());
                    return;
                }
                log.warn("Exception in action handler '{}', retrying ({}/{}): {}",
                        handlerName, attempt, maxRetries, e.getMessage());
            }
        }

        if (result != null && result.isSuccess()) {
            context.putAll(result.getOutput());
            instance.setContextData(serializeContext(context));
            instance.setUpdatedAt(LocalDateTime.now());
            instanceRepository.save(instance);

            execution.setStatus(NodeExecutionStatus.COMPLETED);
            execution.setOutputData(serializeContext(result.getOutput()));
            execution.setCompletedAt(LocalDateTime.now());
            nodeExecutionRepository.save(execution);

            cacheService.cacheWorkflowInstance(instance);

            advanceToNextNode(instance, definition, node, context);
        } else {
            String errorMsg = result != null ? result.getErrorMessage() : "Unknown error";
            handleNodeFailure(instance, execution, errorMsg);
        }
    }

    private void executeForkNode(WorkflowInstance instance, WorkflowDefinition definition,
                                  NodeDefinition node, NodeExecution execution,
                                  Map<String, Object> context) {
        execution.setStatus(NodeExecutionStatus.COMPLETED);
        execution.setCompletedAt(LocalDateTime.now());
        nodeExecutionRepository.save(execution);

        List<TransitionDefinition> outTransitions = transitionDefinitionRepository
                .findByWorkflowDefinitionIdAndSourceNodeKeyOrderByPriorityAsc(
                        definition.getId(), node.getNodeKey());

        log.info("Fork node '{}' spawning {} parallel paths for workflow instance {}",
                node.getNodeKey(), outTransitions.size(), instance.getId());

        for (TransitionDefinition transition : outTransitions) {
            NodeDefinition targetNode = findNodeByKey(definition, transition.getTargetNodeKey());
            logTransition(instance, transition, node.getNodeKey(), targetNode.getNodeKey());
            executeNodeAsync(instance, definition, targetNode);
        }
    }

    private void executeJoinNode(WorkflowInstance instance, WorkflowDefinition definition,
                                  NodeDefinition node, NodeExecution execution,
                                  Map<String, Object> context) {
        // Check if all incoming paths have completed
        List<TransitionDefinition> allTransitions = transitionDefinitionRepository
                .findByWorkflowDefinitionId(definition.getId());

        List<String> incomingNodeKeys = allTransitions.stream()
                .filter(t -> t.getTargetNodeKey().equals(node.getNodeKey()))
                .map(TransitionDefinition::getSourceNodeKey)
                .toList();

        boolean allCompleted = true;
        for (String incomingKey : incomingNodeKeys) {
            List<NodeExecution> executions = nodeExecutionRepository
                    .findByWorkflowInstanceIdAndNodeKey(instance.getId(), incomingKey);
            boolean nodeCompleted = executions.stream()
                    .anyMatch(e -> e.getStatus() == NodeExecutionStatus.COMPLETED);
            if (!nodeCompleted) {
                allCompleted = false;
                break;
            }
        }

        if (allCompleted) {
            execution.setStatus(NodeExecutionStatus.COMPLETED);
            execution.setCompletedAt(LocalDateTime.now());
            nodeExecutionRepository.save(execution);
            advanceToNextNode(instance, definition, node, context);
        } else {
            execution.setStatus(NodeExecutionStatus.PENDING);
            nodeExecutionRepository.save(execution);
            log.debug("Join node '{}' waiting for incoming paths to complete", node.getNodeKey());
        }
    }

    private void advanceToNextNode(WorkflowInstance instance, WorkflowDefinition definition,
                                    NodeDefinition currentNode, Map<String, Object> context) {
        List<TransitionDefinition> transitions = transitionDefinitionRepository
                .findByWorkflowDefinitionIdAndSourceNodeKeyOrderByPriorityAsc(
                        definition.getId(), currentNode.getNodeKey());

        if (transitions.isEmpty()) {
            log.warn("No outgoing transitions from node '{}', workflow instance {} may be stuck",
                    currentNode.getNodeKey(), instance.getId());
            return;
        }

        Optional<TransitionDefinition> matchedTransition =
                transitionEvaluator.evaluateTransitions(transitions, context);

        if (matchedTransition.isPresent()) {
            TransitionDefinition transition = matchedTransition.get();
            NodeDefinition nextNode = findNodeByKey(definition, transition.getTargetNodeKey());

            logTransition(instance, transition, currentNode.getNodeKey(), nextNode.getNodeKey());

            instance.setCurrentNodeKey(nextNode.getNodeKey());
            instance.setUpdatedAt(LocalDateTime.now());
            instanceRepository.save(instance);

            cacheService.cacheWorkflowInstance(instance);

            executeNode(instance, definition, nextNode);
        } else {
            log.warn("No matching transition from node '{}' for workflow instance {}",
                    currentNode.getNodeKey(), instance.getId());
        }
    }

    private void completeWorkflow(WorkflowInstance instance) {
        instance.setStatus(WorkflowStatus.COMPLETED);
        instance.setCompletedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        instanceRepository.save(instance);
        cacheService.cacheWorkflowInstance(instance);
        log.info("Workflow instance {} completed", instance.getId());
    }

    private void handleNodeFailure(WorkflowInstance instance, NodeExecution execution, String errorMessage) {
        execution.setStatus(NodeExecutionStatus.FAILED);
        execution.setErrorMessage(errorMessage);
        execution.setCompletedAt(LocalDateTime.now());
        nodeExecutionRepository.save(execution);

        instance.setStatus(WorkflowStatus.FAILED);
        instance.setUpdatedAt(LocalDateTime.now());
        instanceRepository.save(instance);
        cacheService.cacheWorkflowInstance(instance);

        log.error("Workflow instance {} failed at node '{}': {}",
                instance.getId(), execution.getNodeKey(), errorMessage);
    }

    private NodeExecution createNodeExecution(WorkflowInstance instance, NodeDefinition node) {
        NodeExecution execution = new NodeExecution();
        execution.setWorkflowInstanceId(instance.getId());
        execution.setNodeDefinitionId(node.getId());
        execution.setNodeKey(node.getNodeKey());
        execution.setStatus(NodeExecutionStatus.PENDING);
        execution.setRetryCount(0);
        execution.setStartedAt(LocalDateTime.now());
        execution.setInputData(instance.getContextData());
        return nodeExecutionRepository.save(execution);
    }

    private void logTransition(WorkflowInstance instance, TransitionDefinition transition,
                                String fromKey, String toKey) {
        TransitionLog transitionLog = new TransitionLog();
        transitionLog.setWorkflowInstanceId(instance.getId());
        transitionLog.setTransitionDefinitionId(transition.getId());
        transitionLog.setFromNodeKey(fromKey);
        transitionLog.setToNodeKey(toKey);
        transitionLog.setTransitionedAt(LocalDateTime.now());
        transitionLogRepository.save(transitionLog);
    }

    private NodeDefinition findNodeByKey(WorkflowDefinition definition, String nodeKey) {
        return definition.getNodes().stream()
                .filter(n -> n.getNodeKey().equals(nodeKey))
                .findFirst()
                .orElseThrow(() -> new WorkflowException("Node not found: " + nodeKey));
    }

    private String serializeContext(Map<String, Object> context) {
        if (context == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize context", e);
            return "{}";
        }
    }

    private Map<String, Object> deserializeContext(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize context: {}", json, e);
            return new HashMap<>();
        }
    }
}
