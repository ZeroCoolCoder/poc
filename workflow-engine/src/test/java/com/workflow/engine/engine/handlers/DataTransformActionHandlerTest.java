package com.workflow.engine.engine.handlers;

import com.workflow.engine.engine.ActionResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataTransformActionHandlerTest {

    private final DataTransformActionHandler handler = new DataTransformActionHandler();

    @Test
    void shouldReturnCorrectName() {
        assertEquals("dataTransform", handler.getName());
    }

    @Test
    void shouldTransformDataWithSpelExpressions() {
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", "John");
        context.put("lastName", "Doe");
        context.put("amount", 500);

        Map<String, Object> config = new HashMap<>();
        Map<String, String> transformations = new HashMap<>();
        transformations.put("fullName", "#firstName + ' ' + #lastName");
        transformations.put("isLowValue", "#amount < 1000");
        config.put("transformations", transformations);

        ActionResult result = handler.execute(context, config, new HashMap<>());

        assertTrue(result.isSuccess());
        assertEquals("John Doe", result.getOutput().get("fullName"));
        assertEquals(true, result.getOutput().get("isLowValue"));
    }

    @Test
    void shouldHandleMissingTransformationsConfig() {
        ActionResult result = handler.execute(new HashMap<>(), new HashMap<>(), new HashMap<>());
        assertTrue(result.isSuccess());
    }

    @Test
    void shouldFailOnInvalidExpression() {
        Map<String, Object> context = new HashMap<>();

        Map<String, Object> config = new HashMap<>();
        Map<String, String> transformations = new HashMap<>();
        transformations.put("result", "#nonExistent.invalidMethod()");
        config.put("transformations", transformations);

        ActionResult result = handler.execute(context, config, new HashMap<>());
        assertFalse(result.isSuccess());
    }
}
