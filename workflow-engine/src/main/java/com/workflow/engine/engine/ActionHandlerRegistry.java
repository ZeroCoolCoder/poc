package com.workflow.engine.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for ActionHandler implementations.
 * All beans implementing ActionHandler are auto-registered at startup.
 */
@Component
public class ActionHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(ActionHandlerRegistry.class);

    private final Map<String, ActionHandler> handlers = new ConcurrentHashMap<>();

    public ActionHandlerRegistry(List<ActionHandler> handlerList) {
        for (ActionHandler handler : handlerList) {
            handlers.put(handler.getName(), handler);
            log.info("Registered action handler: {}", handler.getName());
        }
    }

    public Optional<ActionHandler> getHandler(String name) {
        return Optional.ofNullable(handlers.get(name));
    }

    public void registerHandler(ActionHandler handler) {
        handlers.put(handler.getName(), handler);
        log.info("Dynamically registered action handler: {}", handler.getName());
    }

    public boolean hasHandler(String name) {
        return handlers.containsKey(name);
    }

    public Map<String, ActionHandler> getAllHandlers() {
        return Map.copyOf(handlers);
    }
}
