package com.example.sal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public final class JsonUtil {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private JsonUtil() {}

  public static Map<String, Object> parseObject(String json) {
    try {
      return MAPPER.readValue(json, new TypeReference<Map<String, Object>>(){});
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JSON object: " + e.getMessage(), e);
    }
  }

  public static void requireValidJsonObject(String json) {
    parseObject(json);
  }
}
