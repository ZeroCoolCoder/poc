package com.workflow.engine.model.definition;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "WF_DEFINITION")
public class WorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_def_seq")
    @SequenceGenerator(name = "wf_def_seq", sequenceName = "WF_DEFINITION_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private DefinitionStatus status;

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<NodeDefinition> nodes = new HashSet<>();

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TransitionDefinition> transitions = new HashSet<>();

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public WorkflowDefinition() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public DefinitionStatus getStatus() {
        return status;
    }

    public void setStatus(DefinitionStatus status) {
        this.status = status;
    }

    public Set<NodeDefinition> getNodes() {
        return nodes;
    }

    public void setNodes(Set<NodeDefinition> nodes) {
        this.nodes = nodes;
    }

    public Set<TransitionDefinition> getTransitions() {
        return transitions;
    }

    public void setTransitions(Set<TransitionDefinition> transitions) {
        this.transitions = transitions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addNode(NodeDefinition node) {
        nodes.add(node);
        node.setWorkflowDefinition(this);
    }

    public void addTransition(TransitionDefinition transition) {
        transitions.add(transition);
        transition.setWorkflowDefinition(this);
    }
}
