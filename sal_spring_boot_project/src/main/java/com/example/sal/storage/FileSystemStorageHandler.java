package com.example.sal.storage;

import com.example.sal.domain.SalMetadataRecord;
import com.example.sal.domain.SalType;
import com.example.sal.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Component
public class FileSystemStorageHandler implements TypedStorageHandler {

  private final Path root;

  public FileSystemStorageHandler(@Value("${sal.fs.root:./sal-data}") String rootDir) {
    this.root = Path.of(rootDir).toAbsolutePath().normalize();
  }

  @Override public SalType type() { return SalType.FILE_SYSTEM; }

  @Override
  public void saveStream(SalMetadataRecord meta, InputStream in) throws IOException {
    Files.createDirectories(root);
    Path p = resolvePath(meta);
    Files.createDirectories(p.getParent());
    Files.copy(in, p, StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public void readStream(SalMetadataRecord meta, OutputStream out) throws IOException {
    Path p = resolvePath(meta);
    try (InputStream in = Files.newInputStream(p)) {
      in.transferTo(out);
    }
  }

  @Override
  public void delete(SalMetadataRecord meta) throws IOException {
    Files.deleteIfExists(resolvePath(meta));
  }

  private Path resolvePath(SalMetadataRecord meta) {
    String json = meta.getSalMetadataJson();
    if (json != null && !json.isBlank()) {
      Map<String, Object> m = JsonUtil.parseObject(json);
      Object pathVal = m.get("path");
      if (pathVal != null) {
        Path p = Path.of(pathVal.toString());
        return p.isAbsolute() ? p.normalize() : root.resolve(p).normalize();
      }
    }
    String rel = meta.getSalUuid() + "/" + meta.getVersion() + "/" + safe(meta.getSalName());
    return root.resolve(rel).normalize();
  }

  private static String safe(String s) { return s == null ? "object.bin" : s.replaceAll("[\\/]+", "_"); }
}
