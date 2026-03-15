package com.workflow.engine.engine.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link RulesEngineFactory} implementations.
 * All Spring beans implementing {@link RulesEngineFactory} are auto-registered at startup.
 *
 * <p>The registry serves as the entry point for obtaining a {@link RulesEngine} by type.
 * If no type is specified, the default engine ("spel") is returned.</p>
 */
@Component
public class RulesEngineRegistry {

    private static final Logger log = LoggerFactory.getLogger(RulesEngineRegistry.class);
    private static final String DEFAULT_ENGINE_TYPE = "spel";

    private final Map<String, RulesEngineFactory> factories = new ConcurrentHashMap<>();

    public RulesEngineRegistry(List<RulesEngineFactory> factoryList) {
        for (RulesEngineFactory factory : factoryList) {
            factories.put(factory.getType(), factory);
            log.info("Registered rules engine factory: {}", factory.getType());
        }
    }

    /**
     * Gets a rules engine by type. Falls back to the default engine if the
     * requested type is null, blank, or not found.
     *
     * @param type the rules engine type (e.g., "spel", "script")
     * @return the rules engine instance
     */
    public RulesEngine getEngine(String type) {
        String resolvedType = (type == null || type.isBlank()) ? DEFAULT_ENGINE_TYPE : type;
        RulesEngineFactory factory = factories.get(resolvedType);
        if (factory == null) {
            log.warn("Rules engine type '{}' not found, falling back to '{}'", resolvedType, DEFAULT_ENGINE_TYPE);
            factory = factories.get(DEFAULT_ENGINE_TYPE);
            if (factory == null) {
                throw new IllegalStateException("No rules engine registered for type '" + resolvedType
                        + "' and default engine '" + DEFAULT_ENGINE_TYPE + "' is also not available");
            }
        }
        return factory.create();
    }

    /**
     * Returns the factory for a given type, if registered.
     */
    public Optional<RulesEngineFactory> getFactory(String type) {
        return Optional.ofNullable(factories.get(type));
    }

    /**
     * Dynamically registers a new rules engine factory at runtime.
     */
    public void registerFactory(RulesEngineFactory factory) {
        factories.put(factory.getType(), factory);
        log.info("Dynamically registered rules engine factory: {}", factory.getType());
    }

    /**
     * Returns all registered factory types.
     */
    public Map<String, RulesEngineFactory> getAllFactories() {
        return Map.copyOf(factories);
    }
}
