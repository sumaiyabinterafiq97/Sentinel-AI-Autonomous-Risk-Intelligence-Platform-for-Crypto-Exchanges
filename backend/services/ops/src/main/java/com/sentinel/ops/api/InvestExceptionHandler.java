package com.sentinel.ops.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.sentinel.ops.domain.InvestException;

@RestControllerAdvice
public class InvestExceptionHandler {

    private final InvestErrorWriter writer;

    public InvestExceptionHandler(InvestErrorWriter writer) {
        this.writer = writer;
    }

    @ExceptionHandler(InvestException.class)
    public ResponseEntity<Map<String, Object>> handleInvest(InvestException ex) {
        return ResponseEntity.status(ex.httpStatus()).body(writer.body(ex));
    }
}
