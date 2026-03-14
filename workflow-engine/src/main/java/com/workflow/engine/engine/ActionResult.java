package com.workflow.engine.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Result returned from an ActionHandler execution.
 * Contains output data, success/failure status, and optional error info.
 */
public class ActionResult {

    private boolean success;
    private Map<String, Object> output;
    private String errorMessage;

    private ActionResult(boolean success, Map<String, Object> output, String errorMessage) {
        this.success = success;
        this.output = output != null ? output : new HashMap<>();
        this.errorMessage = errorMessage;
    }

    public static ActionResult success(Map<String, Object> output) {
        return new ActionResult(true, output, null);
    }

    public static ActionResult success() {
        return new ActionResult(true, new HashMap<>(), null);
    }

    public static ActionResult failure(String errorMessage) {
        return new ActionResult(false, new HashMap<>(), errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
