package com.workflow.engine.engine.rules;

import com.workflow.engine.model.definition.TransitionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rules engine implementation that uses Spring Expression Language (SpEL)
 * to evaluate transition conditions.
 *
 * <p>This is the default rules engine and adapts the original {@code TransitionEvaluator}
 * logic into the pluggable {@link RulesEngine} contract.</p>
 *
 * <p>Transitions are evaluated in priority order. The first transition whose SpEL
 * condition evaluates to {@code true} is selected. Transitions with no condition
 * are treated as default/unconditional fallbacks.</p>
 */
public class SpelRulesEngine implements RulesEngine {

    private static final Logger log = LoggerFactory.getLogger(SpelRulesEngine.class);

    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public String getType() {
        return "spel";
    }

    @Override
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
                    log.debug("SpEL transition '{}' matched: {} -> {}",
                            transition.getName(), transition.getSourceNodeKey(), transition.getTargetNodeKey());
                    return Optional.of(transition);
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate SpEL transition condition '{}': {}",
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

    @Override
    public boolean evaluateCondition(String conditionExpression, Map<String, Object> context) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            evaluationContext.setVariable(entry.getKey(), entry.getValue());
        }

        Expression expression = parser.parseExpression(conditionExpression);
        Boolean result = expression.getValue(evaluationContext, Boolean.class);
        return Boolean.TRUE.equals(result);
    }
}
