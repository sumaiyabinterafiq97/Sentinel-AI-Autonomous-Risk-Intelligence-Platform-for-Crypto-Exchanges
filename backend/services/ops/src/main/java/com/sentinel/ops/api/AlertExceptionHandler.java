package com.sentinel.ops.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.sentinel.ops.domain.AlertException;

@RestControllerAdvice
public class AlertExceptionHandler {

    private final AlertErrorWriter writer;

    public AlertExceptionHandler(AlertErrorWriter writer) {
        this.writer = writer;
    }

    @ExceptionHandler(AlertException.class)
    public ResponseEntity<Map<String, Object>> handleAlert(AlertException ex) {
        return ResponseEntity.status(ex.httpStatus()).body(writer.body(ex));
    }
}
