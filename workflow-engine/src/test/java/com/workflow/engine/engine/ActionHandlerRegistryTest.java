package com.workflow.engine.engine;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionHandlerRegistryTest {

    @Test
    void shouldRegisterAndRetrieveHandlers() {
        ActionHandler handler1 = new ActionHandler() {
            @Override
            public String getName() { return "test1"; }

            @Override
            public ActionResult execute(Map<String, Object> context, Map<String, Object> nodeConfig, Map<String, Object> input) {
                return ActionResult.success();
            }
        };

        ActionHandler handler2 = new ActionHandler() {
            @Override
            public String getName() { return "test2"; }

            @Override
            public ActionResult execute(Map<String, Object> context, Map<String, Object> nodeConfig, Map<String, Object> input) {
                return ActionResult.success();
            }
        };

        ActionHandlerRegistry registry = new ActionHandlerRegistry(List.of(handler1, handler2));

        assertTrue(registry.hasHandler("test1"));
        assertTrue(registry.hasHandler("test2"));
        assertFalse(registry.hasHandler("nonexistent"));

        Optional<ActionHandler> retrieved = registry.getHandler("test1");
        assertTrue(retrieved.isPresent());
        assertEquals("test1", retrieved.get().getName());
    }

    @Test
    void shouldSupportDynamicRegistration() {
        ActionHandlerRegistry registry = new ActionHandlerRegistry(List.of());

        assertFalse(registry.hasHandler("dynamic"));

        registry.registerHandler(new ActionHandler() {
            @Override
            public String getName() { return "dynamic"; }

            @Override
            public ActionResult execute(Map<String, Object> context, Map<String, Object> nodeConfig, Map<String, Object> input) {
                return ActionResult.success(Map.of("key", "value"));
            }
        });

        assertTrue(registry.hasHandler("dynamic"));
    }
}
