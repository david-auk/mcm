package com.mcm.backend.database.core.factories;

import com.mcm.backend.database.core.annotations.table.Id;
import com.mcm.backend.database.core.annotations.table.TableConstructor;
import com.mcm.backend.database.core.annotations.table.TableField;
import com.mcm.backend.database.core.annotations.table.TableName;
import com.mcm.backend.database.core.components.tables.Table;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class TableFactory<T, K> extends Table<T, K> {

    private final Class<T> clazz;
    private final Field primaryKeyField;
    private final List<Field> columnFields;
    private final Map<Field, String> fieldToColumnName;

    @SuppressWarnings("unchecked")
    public TableFactory(Class<T> clazz) {
        super(
                getTableName(clazz),
                getPrimaryKeyField(clazz).getName(), // Will update to column name below
                (Class<K>) getPrimaryKeyType(clazz),
                buildInsertQuery(clazz),
                buildUpdateQuery(clazz)
        );

        this.clazz = clazz;
        this.primaryKeyField = getPrimaryKeyField(clazz);
        this.columnFields = getColumnFields(clazz);
        this.fieldToColumnName = mapFieldToColumnNames(clazz);

        // override column name if @Id has a column name different than field name
        String idColumnName = fieldToColumnName.get(primaryKeyField);
        try {
            Field tablePrimaryKeyField = Table.class.getDeclaredField("primaryKeyColumnName");
            tablePrimaryKeyField.setAccessible(true);
            tablePrimaryKeyField.set(this, idColumnName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reflectively set primaryKeyColumnName", e);
        }
    }

    private static <T> String getTableName(Class<T> clazz) {
        TableName annotation = clazz.getAnnotation(TableName.class);
        return annotation != null ? annotation.value() : clazz.getSimpleName().toLowerCase();
    }

    private static Field getPrimaryKeyField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalArgumentException("No @Id field found in " + clazz.getName());
    }

    private static Class<?> getPrimaryKeyType(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .map(f -> f.getAnnotation(Id.class).value())
                .orElseThrow(() -> new IllegalArgumentException("Missing @Id annotation with type"));
    }


    private static List<Field> getColumnFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableField.class)) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    private static Map<Field, String> mapFieldToColumnNames(Class<?> clazz) {
        Map<Field, String> map = new LinkedHashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableField.class) || field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                String columnName;
                if (field.isAnnotationPresent(TableField.class)) {
                    columnName = field.getAnnotation(TableField.class).name();
                } else {
                    columnName = field.getName();
                }
                map.put(field, columnName.isEmpty() ? field.getName() : columnName);
            }
        }
        return map;
    }

    private static <T> String buildInsertQuery(Class<T> clazz) {
        Map<Field, String> fieldMap = mapFieldToColumnNames(clazz);
        String tableName = getTableName(clazz);
        String columns = String.join(", ", fieldMap.values());
        String placeholders = fieldMap.values().stream().map(f -> "?").collect(Collectors.joining(", "));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }

    private static <T> String buildUpdateQuery(Class<T> clazz) {
        Map<Field, String> fieldMap = mapFieldToColumnNames(clazz);
        Field idField = getPrimaryKeyField(clazz);
        String idColumn = fieldMap.get(idField);

        String assignments = fieldMap.entrySet().stream()
                .filter(e -> !e.getKey().equals(idField))
                .map(e -> e.getValue() + " = ?")
                .collect(Collectors.joining(", "));

        return String.format("UPDATE %s SET %s WHERE %s = ?", getTableName(clazz), assignments, idColumn);
    }

    @Override
    public void prepareAddStatement(PreparedStatement ps, T entity) throws SQLException {
        try {
            int i = 1;
            for (Field field : fieldToColumnName.keySet()) {
                ps.setObject(i++, field.get(entity));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field", e);
        }
    }

    @Override
    public void prepareUpdateStatement(PreparedStatement ps, T entity) throws SQLException {
        try {
            int i = 1;
            for (Field field : fieldToColumnName.keySet()) {
                if (!field.equals(primaryKeyField)) {
                    ps.setObject(i++, field.get(entity));
                }
            }
            ps.setObject(i, primaryKeyField.get(entity));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field", e);
        }
    }

    @Override
    public T buildFromTableWildcardQuery(ResultSet rs) throws SQLException {
        try {
            Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(c -> c.isAnnotationPresent(TableConstructor.class))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No constructor annotated with @TableConstructor found for " + clazz.getName()));

            constructor.setAccessible(true);

            Field[] fields = clazz.getDeclaredFields();
            Parameter[] params = constructor.getParameters();

            if (params.length != fields.length) {
                throw new RuntimeException("Mismatch between constructor parameters and fields");
            }

            Object[] args = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                Field field = fields[i];
                String columnName = fieldToColumnName.get(field);
                Class<?> fieldType = field.getType();

                try {
                    Object value = rs.getObject(columnName, fieldType);
                    args[i] = value;
                } catch (Exception e) {
                    throw new RuntimeException("Error reading column '" + columnName +
                            "' for field '" + field.getName() + "' of type " + fieldType.getSimpleName(), e);
                }
            }

            return clazz.cast(constructor.newInstance(args));

        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public K getPrimaryKey(T entity) {
        try {
            Object key = primaryKeyField.get(entity);
            if (!primaryKeyDataType.isInstance(key)) {
                throw new IllegalStateException("Primary key type mismatch: expected " + primaryKeyDataType + " but got " + key.getClass());
            }
            return (K) key;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
