package com.mcm.backend.app;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchFieldException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchFieldException(NoSuchFieldException ex) {
        // Log the internal error for debugging purposes
        logger.error("Unexpected reflection error", ex);

        // Hide details from the client
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected internal error occurred."));
    }

    @ExceptionHandler(JsonErrorResponseException.class)
    public ResponseEntity<Map<String, String>> handleJsonErrorResponseException(JsonErrorResponseException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(Map.of("error", ex.getMessage()));
    }
}