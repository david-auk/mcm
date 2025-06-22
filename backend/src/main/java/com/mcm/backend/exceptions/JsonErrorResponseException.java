package com.mcm.backend.exceptions;

import org.springframework.http.HttpStatus;

public class JsonErrorResponseException extends Exception {
    private final HttpStatus status;

    public JsonErrorResponseException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public JsonErrorResponseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}