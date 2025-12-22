package com.example.sal.storage;

import com.example.sal.domain.SalType;

public interface TypedStorageHandler extends StorageHandler {
  SalType type();
}
