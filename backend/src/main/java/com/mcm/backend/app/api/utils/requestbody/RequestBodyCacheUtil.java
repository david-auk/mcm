package com.mcm.backend.app.api.utils.requestbody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestBodyCacheUtil {

    public static final String ATTR_RAW_BODY_MAP = "RAW_BODY_MAP";

    /**
     * Retrieve or parse the raw JSON body into a Map, and cache it in the request.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getOrParseRawBody(HttpServletRequest request, ObjectMapper objectMapper) {
        if (request == null) {
            throw new IllegalStateException("HttpServletRequest is null");
        }

        Map<String, Object> rawMap = (Map<String, Object>) request.getAttribute(ATTR_RAW_BODY_MAP);
        if (rawMap == null) {
            try {
                String json = new BufferedReader(new InputStreamReader(request.getInputStream()))
                        .lines()
                        .collect(Collectors.joining(System.lineSeparator()));
                rawMap = objectMapper.readValue(json, new TypeReference<>() {});
                request.setAttribute(ATTR_RAW_BODY_MAP, rawMap);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse request body", e);
            }
        }
        return rawMap;
    }
}
