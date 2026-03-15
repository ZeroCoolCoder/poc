package com.workflow.engine.engine.rules;

import org.springframework.stereotype.Component;

/**
 * Factory for creating {@link SpelRulesEngine} instances.
 * Registered as the default rules engine factory.
 */
@Component
public class SpelRulesEngineFactory implements RulesEngineFactory {

    @Override
    public String getType() {
        return "spel";
    }

    @Override
    public RulesEngine create() {
        return new SpelRulesEngine();
    }
}
