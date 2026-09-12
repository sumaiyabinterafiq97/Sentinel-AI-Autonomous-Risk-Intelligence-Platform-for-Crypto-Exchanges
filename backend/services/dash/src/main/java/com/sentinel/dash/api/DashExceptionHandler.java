package com.sentinel.dash.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.sentinel.dash.domain.DashException;

@RestControllerAdvice
public class DashExceptionHandler {

    private final DashErrorWriter writer;

    public DashExceptionHandler(DashErrorWriter writer) {
        this.writer = writer;
    }

    @ExceptionHandler(DashException.class)
    public ResponseEntity<Map<String, Object>> handle(DashException ex) {
        return ResponseEntity.status(ex.httpStatus()).body(writer.body(ex));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable() {
        return handle(DashException.validation("body", "Request body is invalid JSON"));
    }
}
