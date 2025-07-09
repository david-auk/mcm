package com.mcm.backend.app.database.core.components.tables;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableColumn;
import com.mcm.backend.app.database.core.annotations.table.TableName;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class TableUtils {

    public static <T> String getTableName(Class<T> clazz) {
        TableName annotation = clazz.getAnnotation(TableName.class);
        return annotation != null ? annotation.value() : clazz.getSimpleName().toLowerCase();
    }

    /**
     * Returns the primary‐key accessor, which may be either:
     *  - a Field annotated with {@link PrimaryKey}, or
     *  - a zero‐arg Method annotated {@link PrimaryKey}
     */
    public static AccessibleObject getPrimaryKeyMember(Class<?> clazz) {
        // 1. look for FIELD @PrimaryKey
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(PrimaryKey.class)) {
                f.setAccessible(true);
                return f;
            }
        }
        // 2. look for METHOD @PrimaryKey
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(PrimaryKey.class)) {
                if (m.getParameterCount() != 0) {
                    throw new IllegalArgumentException(
                            "@PrimaryKey method must be zero‐arg: " + m.getName());
                }
                m.setAccessible(true);
                return m;
            }
        }
        throw new IllegalArgumentException(
                "No @PrimaryKey field or method found in " + clazz.getName());
    }

    /**
     * Return the Class<?> that the @PrimaryKey says this key is.
     */
    public static Class<?> getPrimaryKeyType(Class<?> clazz) {
        AccessibleObject pkMember = getPrimaryKeyMember(clazz);
        if (pkMember instanceof Field) {
            return ((Field) pkMember).getType();
        } else {
            return ((Method) pkMember).getReturnType();
        }
    }

    /**
     * Compute/get the actual PK value from an instance,
     * whether it’s stored in a field or computed by a method.
     */
    public static Object getPrimaryKeyValue(Object instance) {
        AccessibleObject pkMember = getPrimaryKeyMember(instance.getClass());
        try {
            if (pkMember instanceof Field) {
                return ((Field) pkMember).get(instance);
            } else {
                return ((Method) pkMember).invoke(instance);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get primary key value", e);
        }
    }

    public static Map<Field, String> mapFieldToColumnNames(Class<?> clazz) {
        Map<Field, String> map = new LinkedHashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableColumn.class)) {
                field.setAccessible(true);
                String name = field.getAnnotation(TableColumn.class).name();
                String columnName = name.isEmpty() ? field.getName() : name;
                map.put(field, columnName);
            }
        }
        return map;
    }

    /**
     * Return all the fields that should be INSERTed/UPDATEd as non-PK columns.
     * • If the PK is a field, exclude it.
     * • If the PK is a method, include all @TableField fields.
     */
    public static List<Field> getNonPrimaryKeyFields(Class<?> clazz) {
        AccessibleObject pkMember = getPrimaryKeyMember(clazz);

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(TableColumn.class))
                .filter(f -> {
                    // exclude if PK is that same field
                    return !(pkMember instanceof Field && (pkMember).equals(f));
                })
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());
    }

    public static String buildInsertQuery(String tableName, Collection<String> columnNames) {
        String columns = String.join(", ", columnNames);
        String placeholders = String.join(", ", Collections.nCopies(columnNames.size(), "?"));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }

    /**
     * Build an UPDATE statement that:
     *  - sets all non-PK columns
     *  - if PK is a single field, does "WHERE pk = ?"
     *  - if PK is method-based, does "WHERE col1 = ? AND col2 = ? …"
     */
    public static String buildUpdateQuery(Class<?> clazz) {
        String tableName = getTableName(clazz);
        Map<Field, String> fieldToCol = mapFieldToColumnNames(clazz);

        // 1) figure out which columns go in the WHERE clause
        AccessibleObject pkMember = getPrimaryKeyMember(clazz);
        String whereClause;
        if (pkMember instanceof Field) {
            // single-column PK
            String pkCol = fieldToCol.get(pkMember);
            whereClause = pkCol + " = ?";
        } else {
            // method-based PK → treat *all* @TableField fields as the composite key
            whereClause = fieldToCol.values().stream()
                    .map(col -> col + " = ?")
                    .collect(Collectors.joining(" AND "));
        }

        // 2) build the SET assignments from the non-PK fields
        List<Field> nonPk = getNonPrimaryKeyFields(clazz);
        String assignments = nonPk.stream()
                .map(f -> fieldToCol.get(f) + " = ?")
                .collect(Collectors.joining(", "));

        return String.format(
                "UPDATE %s SET %s WHERE %s",
                tableName, assignments, whereClause
        );
    }

}
