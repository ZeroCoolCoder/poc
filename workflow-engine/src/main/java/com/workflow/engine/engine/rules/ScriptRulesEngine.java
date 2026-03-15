package com.workflow.engine.engine.rules;

import com.workflow.engine.model.definition.TransitionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rules engine implementation that uses JSR-223 (Java Scripting API) to evaluate
 * transition conditions. Supports any script language with a JSR-223 provider
 * (JavaScript/Nashorn/GraalJS, Groovy, Python/Jython, etc.).
 *
 * <p>This implementation demonstrates the pluggability of the rules engine abstraction.
 * Condition expressions are evaluated as script code in the configured language,
 * with workflow context variables available as script bindings.</p>
 *
 * <p>Example condition (JavaScript): {@code decision == 'approved' && amount < 10000}</p>
 */
public class ScriptRulesEngine implements RulesEngine {

    private static final Logger log = LoggerFactory.getLogger(ScriptRulesEngine.class);

    private final String language;
    private final ScriptEngineManager scriptEngineManager;

    public ScriptRulesEngine(String language) {
        this.language = language;
        this.scriptEngineManager = new ScriptEngineManager();
    }

    @Override
    public String getType() {
        return "script";
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
                    log.debug("Script ({}) transition '{}' matched: {} -> {}",
                            language, transition.getName(),
                            transition.getSourceNodeKey(), transition.getTargetNodeKey());
                    return Optional.of(transition);
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate script transition condition '{}': {}",
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
        ScriptEngine engine = scriptEngineManager.getEngineByName(language);
        if (engine == null) {
            throw new IllegalStateException("Script engine not found for language: " + language
                    + ". Ensure a JSR-223 provider is on the classpath.");
        }

        Bindings bindings = engine.createBindings();
        bindings.putAll(context);

        try {
            Object result = engine.eval(conditionExpression, bindings);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            return Boolean.parseBoolean(String.valueOf(result));
        } catch (ScriptException e) {
            throw new RuntimeException("Script evaluation failed for expression '"
                    + conditionExpression + "': " + e.getMessage(), e);
        }
    }
}
