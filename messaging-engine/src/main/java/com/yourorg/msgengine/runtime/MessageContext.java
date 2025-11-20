package com.yourorg.msgengine.runtime;

import java.util.HashMap;
import java.util.Map;

public class MessageContext {

    private final String componentId;
    private Object payload;
    private final Map<String, Object> attributes = new HashMap<>();

    public MessageContext(String componentId, Object payload) {
        this.componentId = componentId;
        this.payload = payload;
    }

    public String getComponentId() {
        return componentId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void putAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return type.isInstance(value) ? (T) value : null;
    }
}
