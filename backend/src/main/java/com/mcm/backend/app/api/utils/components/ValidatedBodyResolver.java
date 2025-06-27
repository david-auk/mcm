package com.mcm.backend.app.api.utils.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcm.backend.app.api.utils.RequestBodyUtil;
import com.mcm.backend.app.database.core.annotations.table.Nullable;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;
import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableField;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.exceptions.JsonErrorResponseException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ValidatedBodyResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    public ValidatedBodyResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ValidatedBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory)
            throws JsonErrorResponseException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            throw new IllegalStateException("No HttpServletRequest available.");
        }

        String json = new BufferedReader(new InputStreamReader(servletRequest.getInputStream()))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));

        Map<String, Object> rawMap = objectMapper.readValue(json, new TypeReference<>() {});
        RequestBodyUtil bodyUtil = new RequestBodyUtil(rawMap);

        Class<? extends TableEntity> targetClass = parameter.getParameterAnnotation(ValidatedBody.class).value();
        Constructor<?> tableConstructor = Arrays.stream(targetClass.getConstructors())
                .filter(c -> c.isAnnotationPresent(TableConstructor.class))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No constructor annotated with @TableConstructor"));

        Object[] args = Arrays.stream(tableConstructor.getParameters())
                .map(p -> {
                    try {
                        return resolveParameterValue(p, bodyUtil, targetClass);
                    } catch (JsonErrorResponseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray();

        return tableConstructor.newInstance(args);
    }

    private Object resolveParameterValue(Parameter p, RequestBodyUtil bodyUtil, Class<?> targetClass)
            throws JsonErrorResponseException {

        String paramName = p.getName();
        Field matchingField = getDeclaredFieldSafe(targetClass, paramName);

        // 2) Handle @PrimaryKey: optional, null if absent
        if (matchingField != null && matchingField.isAnnotationPresent(PrimaryKey.class)) {
            PrimaryKey primaryKey = matchingField.getAnnotation(PrimaryKey.class);
            String name = paramName;
            Class<?> type = primaryKey.value();

            if (bodyUtil.containsField(name)) {
                return bodyUtil.getField(name, type);
            } else {
                return null;
            }
        }

        // 3) Handle @TableField
        String name;
        Class<?> type;
        if (matchingField != null && matchingField.isAnnotationPresent(TableField.class)) {
            TableField tableField = matchingField.getAnnotation(TableField.class);
            name = tableField.name().isEmpty() ? paramName : tableField.name();
            type = tableField.type();
        } else {
            // Fallback: JSON key == parameter name
            name = paramName;
            type = p.getType();
        }

        // Validate and return (will throw if missing/invalid)
        try {
            return bodyUtil.getField(name, type);
        } catch (JsonErrorResponseException jsonErrorResponseException) {
            // If the field is not in the body but annotated with AutoGenerated (safe to pass null)
            if (matchingField != null && matchingField.isAnnotationPresent(Nullable.class)) {
                return null;
            } else {
                throw jsonErrorResponseException;
            }
        }
    }

    private Field getDeclaredFieldSafe(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
