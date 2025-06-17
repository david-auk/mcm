package com.mcm.backend.app.database.core.components.tables;

import com.mcm.backend.app.database.core.annotations.table.Id;
import com.mcm.backend.app.database.core.annotations.table.TableField;
import com.mcm.backend.app.database.core.annotations.table.TableName;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class TableUtils {

    public static <T> String getTableName(Class<T> clazz) {
        TableName annotation = clazz.getAnnotation(TableName.class);
        return annotation != null ? annotation.value() : clazz.getSimpleName().toLowerCase();
    }

    public static Field getPrimaryKeyField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .map(f -> {
                    f.setAccessible(true);
                    return f;
                })
                .orElseThrow(() -> new IllegalArgumentException("No @Id field found in " + clazz.getName()));
    }

    public static Class<?> getPrimaryKeyType(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .map(f -> f.getAnnotation(Id.class).value())
                .orElseThrow(() -> new IllegalArgumentException("Missing @Id annotation with type"));
    }

    public static Map<Field, String> mapFieldToColumnNames(Class<?> clazz) {
        Map<Field, String> map = new LinkedHashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableField.class) || field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                String columnName = field.isAnnotationPresent(TableField.class)
                        ? field.getAnnotation(TableField.class).name()
                        : field.getName();
                map.put(field, columnName.isEmpty() ? field.getName() : columnName);
            }
        }
        return map;
    }

    public static List<Field> getNonPrimaryKeyFields(Class<?> clazz) {
        Field pk = getPrimaryKeyField(clazz);
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !f.equals(pk) && (f.isAnnotationPresent(TableField.class)))
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());
    }

    public static String buildInsertQuery(String tableName, Collection<String> columnNames) {
        String columns = String.join(", ", columnNames);
        String placeholders = String.join(", ", Collections.nCopies(columnNames.size(), "?"));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }

    public static String buildUpdateQuery(Class<?> clazz) {
        String tableName = getTableName(clazz);
        Map<Field, String> fieldToColumnName = mapFieldToColumnNames(clazz);
        String primaryKeyColumnName = fieldToColumnName.get(getPrimaryKeyField(clazz));
        List<Field> nonPkFields = getNonPrimaryKeyFields(clazz);

        String assignments = nonPkFields.stream()
                .map(f -> fieldToColumnName.get(f) + " = ?")
                .collect(Collectors.joining(", "));

        return String.format("UPDATE %s SET %s WHERE %s = ?", tableName, assignments, primaryKeyColumnName);
    }


}
