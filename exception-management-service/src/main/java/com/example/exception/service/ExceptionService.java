package com.example.exception.service;

import com.example.exception.api.CreateExceptionRequest;
import com.example.exception.api.UpdateExceptionRequest;
import com.example.exception.domain.ExceptionRecord;
import com.example.exception.domain.ExceptionType;
import com.example.exception.domain.Severity;

import java.util.List;
import java.util.Optional;

public interface ExceptionService {

  ExceptionRecord create(CreateExceptionRequest request);

  ExceptionRecord getByCode(String exceptionCode);

  ExceptionRecord update(String exceptionCode, UpdateExceptionRequest request);

  void delete(String exceptionCode, String lstModUser);

  List<ExceptionRecord> search(Optional<String> type, Optional<String> severity,
                               Optional<Boolean> activeOnly, int limit, int offset);
}
