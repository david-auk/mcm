package com.mcm.backend.app.database.core.components.tables;

import com.mcm.backend.app.database.core.annotations.table.TableConstructor;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class Table<T, K> implements TableInterface<T, K>, AutoTableEntity {

    private final Class<T> clazz;
    private final Field primaryKeyField;
    private final Class<K> primaryKeyDataType;
    private final Map<Field, String> fieldToColumnName;

    protected final String tableName;
    protected final String primaryKeyColumnName;
    protected final String insertQuery;
    protected final String updateQuery;

    @SuppressWarnings("unchecked")
    public Table(Class<T> clazz) {
        this.clazz = clazz;

        this.tableName = TableUtils.getTableName(clazz);
        this.primaryKeyField = TableUtils.getPrimaryKeyField(clazz);
        this.primaryKeyColumnName = TableUtils.mapFieldToColumnNames(clazz).get(primaryKeyField);
        this.primaryKeyDataType = (Class<K>) TableUtils.getPrimaryKeyType(clazz);
        this.fieldToColumnName = TableUtils.mapFieldToColumnNames(clazz);

        this.insertQuery = TableUtils.buildInsertQuery(tableName, fieldToColumnName.values());
        this.updateQuery = TableUtils.buildUpdateQuery(clazz);
    }

    // -- Interface Implementations --

    @Override
    public void prepareInsertStatement(PreparedStatement ps, T entity) throws SQLException {
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
                    args[i] = rs.getObject(columnName, fieldType);
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

    // -- Getters --

    public String getInsertQuery() { return insertQuery; }
    public String getUpdateQuery() { return updateQuery; }
    public String getTableName() { return tableName; }
    public String getPrimaryKeyColumnName() { return primaryKeyColumnName; }

    // -- Debugging --

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Table[").append(clazz.getSimpleName()).append("]:\n");
        sb.append("  Table name: '").append(tableName).append("',\n");
        sb.append("  PK: ").append(primaryKeyField.getName())
                .append(" (").append(primaryKeyDataType.getSimpleName()).append("),\n");
        sb.append("  Fields: [\n");

        for (Map.Entry<Field, String> entry : fieldToColumnName.entrySet()) {
            Field field = entry.getKey();
            if (!field.equals(primaryKeyField)) {
                sb.append("    ")
                        .append(clazz.getSimpleName()).append(".").append(field.getName())
                        .append(" -> ").append(tableName).append(".").append(entry.getValue())
                        .append(": ").append(field.getType().getSimpleName())
                        .append(",\n");
            }
        }
        if (fieldToColumnName.size() - 1 == 0) { // If fields only PK
            sb.setLength(sb.length() - 1); // Remove newline
        } else { // If there are more fields than PK
            sb.setLength(sb.length() - 2); // Remove trailing comma
            sb.append("\n  ");
        }
        sb.append("]");
        return sb.toString();
    }
}

