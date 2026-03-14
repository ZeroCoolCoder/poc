package com.workflow.engine.engine;

import com.workflow.engine.model.definition.TransitionDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransitionEvaluatorTest {

    private TransitionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new TransitionEvaluator();
    }

    @Test
    void shouldEvaluateSimpleCondition() {
        Map<String, Object> context = new HashMap<>();
        context.put("amount", 500);

        boolean result = evaluator.evaluateCondition("#amount < 1000", context);
        assertTrue(result);

        result = evaluator.evaluateCondition("#amount > 1000", context);
        assertFalse(result);
    }

    @Test
    void shouldEvaluateStringCondition() {
        Map<String, Object> context = new HashMap<>();
        context.put("status", "approved");

        boolean result = evaluator.evaluateCondition("#status == 'approved'", context);
        assertTrue(result);

        result = evaluator.evaluateCondition("#status == 'rejected'", context);
        assertFalse(result);
    }

    @Test
    void shouldSelectFirstMatchingTransition() {
        List<TransitionDefinition> transitions = new ArrayList<>();

        TransitionDefinition t1 = new TransitionDefinition();
        t1.setSourceNodeKey("review");
        t1.setTargetNodeKey("approved");
        t1.setConditionExpression("#decision == 'approve'");
        t1.setPriority(1);
        transitions.add(t1);

        TransitionDefinition t2 = new TransitionDefinition();
        t2.setSourceNodeKey("review");
        t2.setTargetNodeKey("rejected");
        t2.setConditionExpression("#decision == 'reject'");
        t2.setPriority(2);
        transitions.add(t2);

        Map<String, Object> context = new HashMap<>();
        context.put("decision", "approve");

        Optional<TransitionDefinition> result = evaluator.evaluateTransitions(transitions, context);
        assertTrue(result.isPresent());
        assertEquals("approved", result.get().getTargetNodeKey());
    }

    @Test
    void shouldUseDefaultTransitionWhenNoConditionsMatch() {
        List<TransitionDefinition> transitions = new ArrayList<>();

        TransitionDefinition t1 = new TransitionDefinition();
        t1.setSourceNodeKey("check");
        t1.setTargetNodeKey("special");
        t1.setConditionExpression("#type == 'special'");
        t1.setPriority(1);
        transitions.add(t1);

        TransitionDefinition defaultTrans = new TransitionDefinition();
        defaultTrans.setSourceNodeKey("check");
        defaultTrans.setTargetNodeKey("normal");
        defaultTrans.setConditionExpression(null);
        defaultTrans.setPriority(2);
        transitions.add(defaultTrans);

        Map<String, Object> context = new HashMap<>();
        context.put("type", "regular");

        Optional<TransitionDefinition> result = evaluator.evaluateTransitions(transitions, context);
        assertTrue(result.isPresent());
        assertEquals("normal", result.get().getTargetNodeKey());
    }

    @Test
    void shouldReturnEmptyWhenNoTransitionsMatch() {
        List<TransitionDefinition> transitions = new ArrayList<>();

        TransitionDefinition t1 = new TransitionDefinition();
        t1.setSourceNodeKey("check");
        t1.setTargetNodeKey("target");
        t1.setConditionExpression("#value > 100");
        t1.setPriority(1);
        transitions.add(t1);

        Map<String, Object> context = new HashMap<>();
        context.put("value", 50);

        Optional<TransitionDefinition> result = evaluator.evaluateTransitions(transitions, context);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldHandleComplexExpressions() {
        Map<String, Object> context = new HashMap<>();
        context.put("amount", 5000);
        context.put("priority", "high");

        boolean result = evaluator.evaluateCondition(
                "#amount > 1000 and #priority == 'high'", context);
        assertTrue(result);

        result = evaluator.evaluateCondition(
                "#amount > 1000 and #priority == 'low'", context);
        assertFalse(result);
    }
}
