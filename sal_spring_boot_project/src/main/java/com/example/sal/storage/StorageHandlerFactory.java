package com.example.sal.storage;

import com.example.sal.domain.SalType;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class StorageHandlerFactory {
  private final Map<SalType, StorageHandler> handlers = new EnumMap<>(SalType.class);

  public StorageHandlerFactory(List<TypedStorageHandler> typedHandlers) {
    for (TypedStorageHandler h : typedHandlers) {
      handlers.put(h.type(), h);
    }
  }

  public StorageHandler get(SalType type) {
    StorageHandler h = handlers.get(type);
    if (h == null) throw new IllegalArgumentException("No handler registered for type=" + type);
    return h;
  }
}
