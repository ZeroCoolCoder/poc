package com.workflow.engine.engine.rules;

/**
 * Abstract factory interface for creating {@link RulesEngine} instances.
 *
 * <p>Each factory implementation is responsible for producing a specific type
 * of rules engine. The factory is identified by a type string that matches
 * the workflow definition's {@code rulesEngineType} configuration.</p>
 *
 * <p>To add a new rules engine implementation:</p>
 * <ol>
 *   <li>Implement {@link RulesEngine} with your engine logic</li>
 *   <li>Implement {@link RulesEngineFactory} to instantiate it</li>
 *   <li>Annotate the factory with {@code @Component} — it will be auto-registered</li>
 * </ol>
 */
public interface RulesEngineFactory {

    /**
     * Returns the type identifier for the rules engines this factory creates.
     * Must match the value used in workflow definition configuration.
     *
     * @return the engine type (e.g., "spel", "script", "drools")
     */
    String getType();

    /**
     * Creates a new {@link RulesEngine} instance.
     *
     * @return a configured rules engine
     */
    RulesEngine create();
}
