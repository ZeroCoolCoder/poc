package com.workflow.engine.engine.handlers;

import com.workflow.engine.engine.ActionHandler;
import com.workflow.engine.engine.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Action handler that makes HTTP calls to external services.
 *
 * Configuration:
 *   - "url": the URL to call (supports ${variable} placeholders from context)
 *   - "method": HTTP method (GET, POST, PUT, DELETE) - defaults to GET
 *   - "headers": map of header name -> value
 *   - "outputKey": key to store response body in context (defaults to "httpResponse")
 */
@Component
public class HttpCallActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpCallActionHandler.class);
    private final RestTemplate restTemplate;

    public HttpCallActionHandler() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getName() {
        return "httpCall";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActionResult execute(Map<String, Object> context, Map<String, Object> nodeConfig, Map<String, Object> input) {
        if (nodeConfig == null || !nodeConfig.containsKey("url")) {
            return ActionResult.failure("HTTP call handler requires 'url' in configuration");
        }

        String url = resolveTemplate((String) nodeConfig.get("url"), context);
        String method = nodeConfig.getOrDefault("method", "GET").toString().toUpperCase();
        String outputKey = nodeConfig.getOrDefault("outputKey", "httpResponse").toString();

        HttpHeaders headers = new HttpHeaders();
        Object headersObj = nodeConfig.get("headers");
        if (headersObj instanceof Map) {
            Map<String, String> headerMap = (Map<String, String>) headersObj;
            headerMap.forEach(headers::set);
        }

        try {
            log.info("HTTP {} {}", method, url);

            HttpMethod httpMethod = HttpMethod.valueOf(method);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);

            Map<String, Object> output = new HashMap<>();
            output.put(outputKey, response.getBody());
            output.put(outputKey + "_status", response.getStatusCode().value());

            log.info("HTTP {} {} -> {}", method, url, response.getStatusCode());
            return ActionResult.success(output);

        } catch (Exception e) {
            log.error("HTTP call failed: {} {}: {}", method, url, e.getMessage());
            return ActionResult.failure("HTTP call failed: " + e.getMessage());
        }
    }

    private String resolveTemplate(String template, Map<String, Object> context) {
        String resolved = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            resolved = resolved.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return resolved;
    }
}
