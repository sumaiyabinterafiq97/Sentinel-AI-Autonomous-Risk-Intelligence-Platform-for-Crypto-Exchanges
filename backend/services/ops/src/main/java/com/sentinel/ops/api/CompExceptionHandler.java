package com.sentinel.ops.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.sentinel.ops.domain.CompException;

@RestControllerAdvice
public class CompExceptionHandler {

    private final CompErrorWriter writer;

    public CompExceptionHandler(CompErrorWriter writer) {
        this.writer = writer;
    }

    @ExceptionHandler(CompException.class)
    public ResponseEntity<Map<String, Object>> handleComp(CompException ex) {
        return ResponseEntity.status(ex.httpStatus()).body(writer.body(ex));
    }
}
