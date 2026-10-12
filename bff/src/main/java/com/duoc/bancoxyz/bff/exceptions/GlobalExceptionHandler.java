package com.duoc.bancoxyz.bff.exceptions;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ClienteNoEncontradoException.class, CuentaNoEncontradaException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleTimeout(ResourceAccessException ex) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(error("BACKEND_TIMEOUT", "bancoxyzBackend no respondio a tiempo"));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleBackend(RestClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(error("BACKEND_ERROR", "Error al consultar bancoxyzBackend"));
    }

    private Map<String, Object> error(String code, String message) {
        return Map.of(
                "code", code,
                "message", message,
                "timestamp", Instant.now().toString());
    }
}
