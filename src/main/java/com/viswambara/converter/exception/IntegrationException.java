package com.viswambara.converter.exception;

import org.springframework.http.HttpStatus;

public class IntegrationException extends RuntimeException {

    private final HttpStatus status;

    public IntegrationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public IntegrationException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
