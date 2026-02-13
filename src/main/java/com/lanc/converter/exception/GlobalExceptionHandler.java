package com.lanc.converter.exception;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ObjectNode> handleIntegration(IntegrationException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus()).body(errorBody(ex.getStatus(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<ObjectNode> handleValidation(Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(errorBody(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ObjectNode> handleUnknown(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI()));
    }

    private ObjectNode errorBody(HttpStatus status, String message, String path) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("status", "ERROR");
        node.put("code", status.value());
        node.put("message", message);
        node.put("path", path);
        return node;
    }
}
