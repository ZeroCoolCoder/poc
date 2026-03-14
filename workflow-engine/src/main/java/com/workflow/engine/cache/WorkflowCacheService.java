package com.workflow.engine.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.engine.model.instance.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed caching layer for hot workflow instance state.
 * Provides fast reads for active workflows while Oracle remains
 * the source of truth.
 */
@Service
public class WorkflowCacheService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowCacheService.class);
    private static final String INSTANCE_KEY_PREFIX = "wf:instance:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public WorkflowCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheWorkflowInstance(WorkflowInstance instance) {
        try {
            String key = INSTANCE_KEY_PREFIX + instance.getId();
            String json = objectMapper.writeValueAsString(instance);
            redisTemplate.opsForValue().set(key, json, DEFAULT_TTL);
            log.debug("Cached workflow instance {}", instance.getId());
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache workflow instance {}: {}", instance.getId(), e.getMessage());
        }
    }

    public Optional<WorkflowInstance> getCachedInstance(Long instanceId) {
        try {
            String key = INSTANCE_KEY_PREFIX + instanceId;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                WorkflowInstance instance = objectMapper.readValue(json, WorkflowInstance.class);
                log.debug("Cache hit for workflow instance {}", instanceId);
                return Optional.of(instance);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to read cached workflow instance {}: {}", instanceId, e.getMessage());
        }
        return Optional.empty();
    }

    public void evictInstance(Long instanceId) {
        String key = INSTANCE_KEY_PREFIX + instanceId;
        redisTemplate.delete(key);
        log.debug("Evicted cached workflow instance {}", instanceId);
    }

    public void cacheWorkflowContext(Long instanceId, String contextJson) {
        String key = INSTANCE_KEY_PREFIX + instanceId + ":context";
        redisTemplate.opsForValue().set(key, contextJson, DEFAULT_TTL);
    }

    public Optional<String> getCachedContext(Long instanceId) {
        String key = INSTANCE_KEY_PREFIX + instanceId + ":context";
        String json = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(json);
    }
}
