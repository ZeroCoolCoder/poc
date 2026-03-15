package com.workflow.engine.engine.rules;

import org.springframework.stereotype.Component;

/**
 * Factory for creating {@link ScriptRulesEngine} instances.
 *
 * <p>Produces script-based rules engines using JSR-223.
 * The default scripting language is JavaScript, but any JSR-223 compliant
 * language can be used by providing a different language name.</p>
 */
@Component
public class ScriptRulesEngineFactory implements RulesEngineFactory {

    private static final String DEFAULT_LANGUAGE = "js";

    @Override
    public String getType() {
        return "script";
    }

    @Override
    public RulesEngine create() {
        return new ScriptRulesEngine(DEFAULT_LANGUAGE);
    }
}
