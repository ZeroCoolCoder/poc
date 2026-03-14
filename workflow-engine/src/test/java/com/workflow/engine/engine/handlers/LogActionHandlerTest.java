package com.workflow.engine.engine.handlers;

import com.workflow.engine.engine.ActionResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogActionHandlerTest {

    private final LogActionHandler handler = new LogActionHandler();

    @Test
    void shouldReturnCorrectName() {
        assertEquals("log", handler.getName());
    }

    @Test
    void shouldLogMessageWithTemplateResolution() {
        Map<String, Object> context = new HashMap<>();
        context.put("orderId", "ORD-123");
        context.put("customerName", "John Doe");

        Map<String, Object> config = Map.of(
                "message", "Processing order ${orderId} for ${customerName}",
                "level", "INFO"
        );

        ActionResult result = handler.execute(context, config, new HashMap<>());

        assertTrue(result.isSuccess());
        assertEquals("Processing order ORD-123 for John Doe", result.getOutput().get("logged_message"));
    }

    @Test
    void shouldHandleNullConfig() {
        ActionResult result = handler.execute(new HashMap<>(), null, new HashMap<>());
        assertTrue(result.isSuccess());
    }
}
