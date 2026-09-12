package com.sentinel.ops.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.sentinel.ops.domain.AlertException;
import com.sentinel.ops.domain.CompException;
import com.sentinel.ops.domain.InvestException;
import com.sentinel.ops.domain.RiskException;

@RestControllerAdvice
public class RiskExceptionHandler {

    private final RiskErrorWriter writer;

    public RiskExceptionHandler(RiskErrorWriter writer) {
        this.writer = writer;
    }

    @ExceptionHandler(RiskException.class)
    public ResponseEntity<Map<String, Object>> handleRisk(RiskException ex) {
        return ResponseEntity.status(ex.httpStatus()).body(writer.body(ex));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return handleRisk(RiskException.validation("body", "Request body is invalid JSON"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleType(MethodArgumentTypeMismatchException ex) {
        return handleRisk(RiskException.validation(ex.getName(), "Invalid parameter"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex) {
        if (ex instanceof AlertException alert) {
            return ResponseEntity.status(alert.httpStatus()).body(new AlertErrorWriter().body(alert));
        }
        if (ex instanceof InvestException invest) {
            return ResponseEntity.status(invest.httpStatus()).body(new InvestErrorWriter().body(invest));
        }
        if (ex instanceof CompException comp) {
            return ResponseEntity.status(comp.httpStatus()).body(new CompErrorWriter().body(comp));
        }
        return handleRisk(RiskException.internal());
    }
}
