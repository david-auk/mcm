package com.mcm.backend.app.routes.utils;

import com.mcm.backend.exceptions.JsonErrorResponseException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RequestBodyUtil {

    final Map<String, ?> requestBody;

    public RequestBodyUtil(Map<String, ?> requestBody) {
        this.requestBody = requestBody;
    }

    public <T> T getField(String fieldName, Class<T> fieldType) throws JsonErrorResponseException {

        Object fieldValue = requestBody.get(fieldName);

        if (fieldValue == null) {
            throw new JsonErrorResponseException(fieldName + " is required");
        }

        if (fieldType.isInstance(fieldValue)) {
            return fieldType.cast(fieldValue);
        } else if (fieldType == Double.class && fieldValue instanceof Integer) { // Convert Integer to Double
            return fieldType.cast(((Integer) fieldValue).doubleValue());
        } else if (fieldType == Timestamp.class && fieldValue instanceof String timestampString) { // Convert String to Timestamp
            return fieldType.cast(convertStringToTimestamp(timestampString));
        } else {
            throw new JsonErrorResponseException(fieldName + " is not of type " + fieldType.getName());
        }
    }

    public boolean containsField(String fieldName) {
        return requestBody.containsKey(fieldName);
    }

    // --- Helper methods ---

    private Timestamp convertStringToTimestamp(String timestampString) throws JsonErrorResponseException {
        // List of supported timestamp formats
        List<String> formats = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm",    // Example: 2025-01-13T16:00
                "yyyy-MM-dd HH:mm:ss",   // Example: 2025-01-13 16:00:00
                "yyyy-MM-dd HH:mm",      // Example: 2025-01-13 16:00
                "yyyy/MM/dd HH:mm:ss",   // Example: 2025/01/13 16:00:00
                "yyyy/MM/dd HH:mm"       // Example: 2025/01/13 16:00
        );

        // Try parsing the string with each format
        for (String format : formats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                java.util.Date parsedDate = dateFormat.parse(timestampString);
                return new Timestamp(parsedDate.getTime());
            } catch (ParseException e) {
                // Continue to the next format if parsing fails
            }
        }

        // If none of the formats worked, throw an exception
        throw new JsonErrorResponseException(timestampString + " is not a valid timestamp format. Supported formats: " + formats);
    }
}