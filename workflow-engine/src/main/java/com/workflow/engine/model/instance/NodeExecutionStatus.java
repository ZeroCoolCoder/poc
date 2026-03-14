package com.workflow.engine.model.instance;

public enum NodeExecutionStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    WAITING_FOR_INPUT,
    SKIPPED
}
