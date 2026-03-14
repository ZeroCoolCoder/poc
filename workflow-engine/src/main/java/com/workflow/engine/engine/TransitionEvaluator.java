package com.workflow.engine.engine;

import com.workflow.engine.model.definition.TransitionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Evaluates transition conditions using Spring Expression Language (SpEL).
 * Transitions are evaluated in priority order; the first matching transition is selected.
 * A transition with no condition expression is treated as a default/unconditional transition.
 */
@Component
public class TransitionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(TransitionEvaluator.class);

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluates a list of transitions against the current workflow context
     * and returns the first matching transition.
     *
     * @param transitions the transitions to evaluate (should be ordered by priority)
     * @param context     the current workflow context
     * @return the first matching transition, or empty if none match
     */
    public Optional<TransitionDefinition> evaluateTransitions(
            List<TransitionDefinition> transitions, Map<String, Object> context) {

        TransitionDefinition defaultTransition = null;

        for (TransitionDefinition transition : transitions) {
            String condition = transition.getConditionExpression();

            if (condition == null || condition.isBlank()) {
                if (defaultTransition == null) {
                    defaultTransition = transition;
                }
                continue;
            }

            try {
                if (evaluateCondition(condition, context)) {
                    log.debug("Transition '{}' matched: {} -> {}",
                            transition.getName(), transition.getSourceNodeKey(), transition.getTargetNodeKey());
                    return Optional.of(transition);
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate transition condition '{}': {}",
                        condition, e.getMessage());
            }
        }

        if (defaultTransition != null) {
            log.debug("Using default transition: {} -> {}",
                    defaultTransition.getSourceNodeKey(), defaultTransition.getTargetNodeKey());
            return Optional.of(defaultTransition);
        }

        return Optional.empty();
    }

    /**
     * Evaluates a SpEL condition expression against the given context.
     */
    public boolean evaluateCondition(String conditionExpression, Map<String, Object> context) {
        EvaluationContext evaluationContext = new StandardEvaluationContext();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            ((StandardEvaluationContext) evaluationContext).setVariable(entry.getKey(), entry.getValue());
        }

        Expression expression = parser.parseExpression(conditionExpression);
        Boolean result = expression.getValue(evaluationContext, Boolean.class);
        return Boolean.TRUE.equals(result);
    }
}
