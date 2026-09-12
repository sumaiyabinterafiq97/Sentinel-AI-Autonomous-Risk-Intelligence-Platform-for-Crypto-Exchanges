package com.sentinel.identity.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.sentinel.identity.domain.IdentityException;

@RestControllerAdvice
public class IdentityExceptionHandler {

    private final IdentityErrorWriter writer;

    public IdentityExceptionHandler(IdentityErrorWriter writer) {
        this.writer = writer;
    }

    @ExceptionHandler(IdentityException.class)
    public ResponseEntity<Map<String, Object>> handle(IdentityException ex) {
        return ResponseEntity.status(ex.httpStatus()).body(writer.body(ex));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unread(HttpMessageNotReadableException ex) {
        return handle(IdentityException.validation("body", "Request body is invalid JSON"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unknown(Exception ex) {
        return handle(new IdentityException(500, "PLATFORM_INTERNAL_001", "An internal error occurred", true, null));
    }
}
