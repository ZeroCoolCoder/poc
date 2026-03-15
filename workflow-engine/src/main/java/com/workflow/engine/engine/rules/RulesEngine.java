package com.workflow.engine.engine.rules;

import com.workflow.engine.model.definition.TransitionDefinition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract contract for evaluating transition rules in the workflow engine.
 * Implementations determine which outgoing transition should be taken based on
 * the current workflow context.
 *
 * <p>This interface follows the Adapter pattern — each implementation adapts
 * a specific rules engine technology (SpEL, Drools, Easy Rules, custom logic, etc.)
 * to the workflow engine's transition evaluation contract.</p>
 */
public interface RulesEngine {

    /**
     * Returns the unique type identifier for this rules engine implementation.
     * Used to select the appropriate engine based on workflow definition configuration.
     *
     * @return the engine type name (e.g., "spel", "script", "drools")
     */
    String getType();

    /**
     * Evaluates a list of transitions against the current workflow context
     * and returns the first matching transition.
     *
     * <p>Transitions should be evaluated in priority order. A transition with no
     * condition expression should be treated as a default/unconditional transition,
     * selected only if no conditional transition matches.</p>
     *
     * @param transitions the candidate transitions to evaluate (ordered by priority)
     * @param context     the current workflow context (shared mutable state)
     * @return the first matching transition, or empty if none match
     */
    Optional<TransitionDefinition> evaluateTransitions(
            List<TransitionDefinition> transitions, Map<String, Object> context);

    /**
     * Evaluates a single condition expression against the given context.
     *
     * @param conditionExpression the condition to evaluate
     * @param context             the current workflow context
     * @return true if the condition is satisfied, false otherwise
     */
    boolean evaluateCondition(String conditionExpression, Map<String, Object> context);
}
