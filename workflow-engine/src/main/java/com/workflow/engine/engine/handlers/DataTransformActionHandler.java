package com.workflow.engine.engine.handlers;

import com.workflow.engine.engine.ActionHandler;
import com.workflow.engine.engine.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Action handler that transforms data in the workflow context using SpEL expressions.
 *
 * Configuration:
 *   - "transformations": a map of output_key -> SpEL expression
 *
 * Example config:
 * {
 *   "transformations": {
 *     "fullName": "#firstName + ' ' + #lastName",
 *     "isApproved": "#amount < 1000"
 *   }
 * }
 */
@Component
public class DataTransformActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(DataTransformActionHandler.class);
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public String getName() {
        return "dataTransform";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActionResult execute(Map<String, Object> context, Map<String, Object> nodeConfig, Map<String, Object> input) {
        if (nodeConfig == null || !nodeConfig.containsKey("transformations")) {
            return ActionResult.success();
        }

        Object transformationsObj = nodeConfig.get("transformations");
        if (!(transformationsObj instanceof Map)) {
            return ActionResult.failure("'transformations' config must be a map");
        }

        Map<String, String> transformations = (Map<String, String>) transformationsObj;
        Map<String, Object> output = new HashMap<>();

        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            evalContext.setVariable(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> transformation : transformations.entrySet()) {
            try {
                Object result = parser.parseExpression(transformation.getValue()).getValue(evalContext);
                output.put(transformation.getKey(), result);
                log.debug("Transform '{}' = {} -> {}", transformation.getKey(), transformation.getValue(), result);
            } catch (Exception e) {
                log.warn("Failed to evaluate transformation '{}': {}", transformation.getKey(), e.getMessage());
                return ActionResult.failure("Transform failed for '" + transformation.getKey() + "': " + e.getMessage());
            }
        }

        return ActionResult.success(output);
    }
}
