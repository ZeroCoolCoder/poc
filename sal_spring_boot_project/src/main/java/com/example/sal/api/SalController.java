package com.example.sal.api;

import com.example.sal.domain.SalMetadataRecord;
import com.example.sal.service.SalService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/sal")
public class SalController {

  private final SalService salService;
  public SalController(SalService salService) { this.salService = salService; }

  private String subject(String hdr) { return (hdr == null || hdr.isBlank()) ? "anonymous" : hdr; }

  @PostMapping(value="/objects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CreateSalResponse> create(@RequestPart("meta") @Valid CreateSalRequest meta,
                                                  @RequestPart("file") org.springframework.web.multipart.MultipartFile file,
                                                  @RequestHeader(value="X-Subject", required=false) String subject) throws IOException {
    try (InputStream in = file.getInputStream()) {
      var r = salService.create(meta, subject(subject), in);
      return ResponseEntity.ok(new CreateSalResponse(r.getSalUuid(), r.getVersion()));
    }
  }

  @PostMapping(value="/objects/{salUuid}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CreateSalResponse> createVersion(@PathVariable String salUuid,
                                                         @RequestPart("meta") @Valid CreateSalRequest meta,
                                                         @RequestPart("file") org.springframework.web.multipart.MultipartFile file,
                                                         @RequestHeader(value="X-Subject", required=false) String subject) throws IOException {
    try (InputStream in = file.getInputStream()) {
      var r = salService.createNewVersion(salUuid, meta, subject(subject), in);
      return ResponseEntity.ok(new CreateSalResponse(r.getSalUuid(), r.getVersion()));
    }
  }

  @GetMapping("/objects/{salUuid}/latest")
  public SalMetadataRecord latestMeta(@PathVariable String salUuid,
                                      @RequestHeader(value="X-Subject", required=false) String subject) {
    return salService.getLatest(salUuid, subject(subject));
  }

  @GetMapping("/objects/{salUuid}/versions/{version}")
  public SalMetadataRecord versionMeta(@PathVariable String salUuid,
                                       @PathVariable long version,
                                       @RequestHeader(value="X-Subject", required=false) String subject) {
    return salService.getVersion(salUuid, version, subject(subject));
  }

  @GetMapping("/objects/{salUuid}/latest/stream")
  public void downloadLatest(@PathVariable String salUuid,
                             @RequestHeader(value="X-Subject", required=false) String subject,
                             HttpServletResponse resp) throws IOException {
    resp.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    salService.downloadLatest(salUuid, subject(subject), resp.getOutputStream());
  }

  @GetMapping("/objects/{salUuid}/versions/{version}/stream")
  public void downloadVersion(@PathVariable String salUuid,
                              @PathVariable long version,
                              @RequestHeader(value="X-Subject", required=false) String subject,
                              HttpServletResponse resp) throws IOException {
    resp.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    salService.downloadVersion(salUuid, version, subject(subject), resp.getOutputStream());
  }

  @GetMapping("/objects/search")
  public List<SalMetadataRecord> search(@RequestParam Optional<String> name,
                                       @RequestParam Optional<String> ownerId,
                                       @RequestParam Optional<LocalDate> from,
                                       @RequestParam Optional<LocalDate> to,
                                       @RequestParam Optional<String> type,
                                       @RequestParam Optional<Boolean> latestOnly,
                                       @RequestParam(defaultValue="50") int limit,
                                       @RequestParam(defaultValue="0") int offset,
                                       @RequestHeader(value="X-Subject", required=false) String subject) {
    return salService.search(name, ownerId, from, to, type, latestOnly, limit, offset, subject(subject));
  }
}
