package com.example.exception.service;

import com.example.exception.domain.ExceptionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ExceptionCacheService {

  private static final Logger log = LoggerFactory.getLogger(ExceptionCacheService.class);
  private static final String CACHE_PREFIX = "exc:";
  private static final long TTL_HOURS = 24;

  private final RedisTemplate<String, ExceptionRecord> redisTemplate;
  private final boolean redisAvailable;

  public ExceptionCacheService(RedisTemplate<String, ExceptionRecord> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.redisAvailable = testConnection();
  }

  private boolean testConnection() {
    try {
      redisTemplate.getConnectionFactory().getConnection().ping();
      log.info("Redis connection established - caching enabled");
      return true;
    } catch (Exception e) {
      log.warn("Redis not available - caching disabled. Reason: {}", e.getMessage());
      return false;
    }
  }

  public ExceptionRecord get(String exceptionCode) {
    if (!redisAvailable) return null;
    try {
      return redisTemplate.opsForValue().get(CACHE_PREFIX + exceptionCode);
    } catch (Exception e) {
      log.warn("Redis GET failed for {}: {}", exceptionCode, e.getMessage());
      return null;
    }
  }

  public void put(ExceptionRecord record) {
    if (!redisAvailable) return;
    try {
      redisTemplate.opsForValue().set(
          CACHE_PREFIX + record.getExceptionCode(), record, TTL_HOURS, TimeUnit.HOURS);
    } catch (Exception e) {
      log.warn("Redis PUT failed for {}: {}", record.getExceptionCode(), e.getMessage());
    }
  }

  public void evict(String exceptionCode) {
    if (!redisAvailable) return;
    try {
      redisTemplate.delete(CACHE_PREFIX + exceptionCode);
    } catch (Exception e) {
      log.warn("Redis DELETE failed for {}: {}", exceptionCode, e.getMessage());
    }
  }
}
