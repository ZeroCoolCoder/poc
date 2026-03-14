package com.workflow.engine.engine.handlers;

import com.workflow.engine.engine.ActionHandler;
import com.workflow.engine.engine.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simple action handler that logs messages.
 * Useful for debugging and auditing workflow execution.
 *
 * Configuration:
 *   - "message": the message template to log
 *   - "level": log level (INFO, DEBUG, WARN) - defaults to INFO
 */
@Component
public class LogActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(LogActionHandler.class);

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public ActionResult execute(Map<String, Object> context, Map<String, Object> nodeConfig, Map<String, Object> input) {
        String message = nodeConfig != null ? (String) nodeConfig.getOrDefault("message", "Workflow log event") : "Workflow log event";
        String level = nodeConfig != null ? (String) nodeConfig.getOrDefault("level", "INFO") : "INFO";

        String resolvedMessage = resolveTemplate(message, context);

        switch (level.toUpperCase()) {
            case "DEBUG":
                log.debug("[WorkflowLog] {}", resolvedMessage);
                break;
            case "WARN":
                log.warn("[WorkflowLog] {}", resolvedMessage);
                break;
            default:
                log.info("[WorkflowLog] {}", resolvedMessage);
                break;
        }

        return ActionResult.success(Map.of("logged_message", resolvedMessage));
    }

    private String resolveTemplate(String template, Map<String, Object> context) {
        String resolved = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            resolved = resolved.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return resolved;
    }
}
