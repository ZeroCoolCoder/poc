package com.workflow.engine.service;

import com.workflow.engine.dto.request.CreateWorkflowDefinitionRequest;
import com.workflow.engine.dto.request.NodeDefinitionRequest;
import com.workflow.engine.dto.request.TransitionDefinitionRequest;
import com.workflow.engine.exception.WorkflowException;
import com.workflow.engine.model.definition.NodeType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowDefinitionServiceTest {

    @Test
    void shouldRejectDefinitionWithoutStartNode() {
        CreateWorkflowDefinitionRequest request = new CreateWorkflowDefinitionRequest();
        request.setName("test-workflow");

        List<NodeDefinitionRequest> nodes = new ArrayList<>();
        NodeDefinitionRequest endNode = new NodeDefinitionRequest();
        endNode.setNodeKey("end");
        endNode.setName("End");
        endNode.setNodeType(NodeType.END);
        nodes.add(endNode);

        request.setNodes(nodes);
        request.setTransitions(new ArrayList<>());

        WorkflowDefinitionService service = new WorkflowDefinitionService(null);
        assertThrows(WorkflowException.class, () -> service.createDefinition(request));
    }

    @Test
    void shouldBuildValidRequestWithStartAndEndNodes() {
        CreateWorkflowDefinitionRequest request = createValidRequest();

        // Verify the request has proper structure
        assert request.getNodes().size() == 2;
        assert request.getTransitions().size() == 1;
        assert request.getNodes().stream().anyMatch(n -> n.getNodeType() == NodeType.START);
        assert request.getNodes().stream().anyMatch(n -> n.getNodeType() == NodeType.END);
    }

    private CreateWorkflowDefinitionRequest createValidRequest() {
        CreateWorkflowDefinitionRequest request = new CreateWorkflowDefinitionRequest();
        request.setName("test-workflow");
        request.setDescription("A test workflow");

        List<NodeDefinitionRequest> nodes = new ArrayList<>();

        NodeDefinitionRequest startNode = new NodeDefinitionRequest();
        startNode.setNodeKey("start");
        startNode.setName("Start Node");
        startNode.setNodeType(NodeType.START);
        nodes.add(startNode);

        NodeDefinitionRequest endNode = new NodeDefinitionRequest();
        endNode.setNodeKey("end");
        endNode.setName("End Node");
        endNode.setNodeType(NodeType.END);
        nodes.add(endNode);

        request.setNodes(nodes);

        List<TransitionDefinitionRequest> transitions = new ArrayList<>();
        TransitionDefinitionRequest transition = new TransitionDefinitionRequest();
        transition.setSourceNodeKey("start");
        transition.setTargetNodeKey("end");
        transitions.add(transition);

        request.setTransitions(transitions);

        return request;
    }
}
