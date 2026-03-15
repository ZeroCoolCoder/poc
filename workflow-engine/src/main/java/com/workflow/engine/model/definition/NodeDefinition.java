package com.workflow.engine.model.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "WF_NODE_DEFINITION")
public class NodeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_node_def_seq")
    @SequenceGenerator(name = "wf_node_def_seq", sequenceName = "WF_NODE_DEF_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "NODE_KEY", nullable = false, length = 100)
    private String nodeKey;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "NODE_TYPE", nullable = false, length = 20)
    private NodeType nodeType;

    @Column(name = "ACTION_HANDLER", length = 255)
    private String actionHandler;

    @Column(name = "CONFIGURATION", length = 4000)
    private String configuration;

    @Column(name = "TIMEOUT_SECONDS")
    private Long timeoutSeconds;

    @Column(name = "MAX_RETRIES")
    private Integer maxRetries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_DEFINITION_ID", nullable = false)
    @JsonIgnore
    private WorkflowDefinition workflowDefinition;

    public NodeDefinition() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getActionHandler() {
        return actionHandler;
    }

    public void setActionHandler(String actionHandler) {
        this.actionHandler = actionHandler;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public Long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public WorkflowDefinition getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeDefinition that = (NodeDefinition) o;
        return Objects.equals(nodeKey, that.nodeKey) &&
               Objects.equals(workflowDefinition != null ? workflowDefinition.getId() : null,
                              that.workflowDefinition != null ? that.workflowDefinition.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeKey);
    }
}
