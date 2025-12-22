package com.example.sal.storage;

import com.example.sal.domain.SalMetadataRecord;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StorageHandler {
  void saveStream(SalMetadataRecord meta, InputStream in) throws IOException;
  void readStream(SalMetadataRecord meta, OutputStream out) throws IOException;
  void delete(SalMetadataRecord meta) throws IOException;
}
