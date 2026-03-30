package com.example.exception.api;

import com.example.exception.domain.ExceptionRecord;
import com.example.exception.service.ExceptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/exceptions")
public class ExceptionController {

  private final ExceptionService exceptionService;

  public ExceptionController(ExceptionService exceptionService) {
    this.exceptionService = exceptionService;
  }

  @PostMapping
  public ResponseEntity<ExceptionRecord> create(@RequestBody @Valid CreateExceptionRequest request) {
    ExceptionRecord created = exceptionService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/{code}")
  public ResponseEntity<ExceptionRecord> getByCode(@PathVariable String code) {
    ExceptionRecord record = exceptionService.getByCode(code);
    return ResponseEntity.ok(record);
  }

  @GetMapping
  public ResponseEntity<List<ExceptionRecord>> search(
      @RequestParam Optional<String> type,
      @RequestParam Optional<String> severity,
      @RequestParam Optional<Boolean> activeOnly,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    List<ExceptionRecord> results = exceptionService.search(type, severity, activeOnly, limit, offset);
    return ResponseEntity.ok(results);
  }

  @PutMapping("/{code}")
  public ResponseEntity<ExceptionRecord> update(@PathVariable String code,
                                                @RequestBody @Valid UpdateExceptionRequest request) {
    ExceptionRecord updated = exceptionService.update(code, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{code}")
  public ResponseEntity<Void> delete(@PathVariable String code,
                                     @RequestParam String lstModUser) {
    exceptionService.delete(code, lstModUser);
    return ResponseEntity.noContent().build();
  }
}
