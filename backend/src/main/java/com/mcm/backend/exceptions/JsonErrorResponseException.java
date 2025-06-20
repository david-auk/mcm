package com.mcm.backend.exceptions;

public class JsonErrorResponseException extends Exception {
    public JsonErrorResponseException(String message) {
        super(message);
    }
}