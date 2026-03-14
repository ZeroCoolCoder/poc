package com.workflow.engine.engine;

import java.util.Map;

/**
 * Interface for pluggable action handlers that execute business logic at each node.
 * Implementations are registered by name and invoked by the workflow engine
 * when an AUTO node is reached.
 */
public interface ActionHandler {

    /**
     * Returns the unique name used to reference this handler in node definitions.
     */
    String getName();

    /**
     * Executes the business logic for a given node.
     *
     * @param context    the current workflow context (shared mutable state)
     * @param nodeConfig the node-specific configuration (JSON parsed to map)
     * @param input      the input data for this execution
     * @return the result of the execution, merged back into the workflow context
     */
    ActionResult execute(Map<String, Object> context, Map<String, Object> nodeConfig, Map<String, Object> input);
}
