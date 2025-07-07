package com.mcm.backend.app.api.utils.requestbody;

import com.mcm.backend.exceptions.JsonErrorResponseException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RequestBodyUtil {

    final Map<String, ?> requestBody;

    public RequestBodyUtil(Map<String, ?> requestBody) {
        this.requestBody = requestBody;
    }

    public boolean containsField(String fieldName) {
        return requestBody.containsKey(fieldName);
    }

    /**
     * Retrieves a required field from the request body.
     * @param fieldName the name of the field to retrieve
     * @param fieldType the expected type of the field
     * @param <T> the type of the returned field
     * @return the field value cast to the specified type
     * @throws JsonErrorResponseException if the field is missing or cannot be converted to the specified type
     */
    public <T> T getField(String fieldName, Class<T> fieldType) throws JsonErrorResponseException {
        T fieldValue = getOptionalField(fieldName, fieldType);

        if (fieldValue == null) {
            throw new JsonErrorResponseException(fieldName + " is required");
        }
        return fieldValue;
    }

    /**
     * Retrieves an optional field from the request body.
     * @param fieldName the name of the field
     * @param fieldType the expected type of the field
     * @param <T> type parameter
     * @return the field value cast to the specified type, or null if the field is not present or is null
     * @throws JsonErrorResponseException if the field is present but cannot be converted to the specified type
     */
    public <T> T getOptionalField(String fieldName, Class<T> fieldType) throws JsonErrorResponseException {
        if (!requestBody.containsKey(fieldName)) {
            return null;
        }
        Object fieldValue = requestBody.get(fieldName);
        if (fieldValue == null) {
            return null;
        }
        return castFieldValue(fieldName, fieldValue, fieldType);
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

    /**
     * Casts and converts a field value to the specified type, throwing if incompatible.
     */
    private <T> T castFieldValue(String fieldName, Object fieldValue, Class<T> fieldType) throws JsonErrorResponseException {
        if (fieldType.isInstance(fieldValue)) {
            return fieldType.cast(fieldValue);
        } else if (fieldType == Double.class && fieldValue instanceof Integer) {
            return fieldType.cast(((Integer) fieldValue).doubleValue());
        } else if (fieldType == Timestamp.class && fieldValue instanceof String timestampString) {
            return fieldType.cast(convertStringToTimestamp(timestampString));
        } else if (fieldType == UUID.class && fieldValue instanceof String uuidString) {
            try {
                return fieldType.cast(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                throw new JsonErrorResponseException(fieldName + " is not a valid UUID");
            }
        } else {
            throw new JsonErrorResponseException(fieldName + " is not of type " + fieldType.getName());
        }
    }
}