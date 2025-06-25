package com.mcm.backend.app.database.core.components.tables;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.components.daos.querying.FilterCriterion;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class Table<T, K> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    /**
     * Build a SELECT … WHERE … [AND …] [ORDER BY …] query,
     * using the given filter list and sort.
     *
     * @param filters       list of FilterCriterion; null-valued criteria are skipped
     * @param orderByField  optional Field to ORDER BY
     * @param ascending     true for ASC, false for DESC
     * @return the SQL string with “?” placeholders for each non-null value
     * @throws IllegalArgumentException if any Field isn’t part of this table’s entity
     */
    public String buildGetQuery(
            List<FilterCriterion<?>> filters,
            Field orderByField,
            boolean ascending
    ) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ")
                .append(tableName);

        boolean first = true;
        for (FilterCriterion<?> criterion : filters) {
            Field f = criterion.getField();
            Object v = criterion.getValue();

            // skip null filters
            if (v == null) continue;

            // validate field belongs to this entity
            if (!f.getDeclaringClass().equals(clazz)) {
                throw new IllegalArgumentException(
                        "Field " + f.getName() + " not from " + clazz.getName());
            }

            String col = fieldToColumnName.get(f);
            if (col == null) {
                throw new IllegalArgumentException(
                        "Missing field " + f.getName() + " in table " + tableName);
            }

            sql.append(first ? " WHERE " : " AND ")
                    .append(col)
                    .append(criterion.isWildcard() ? " LIKE ?" : " = ?");
            first = false;
        }

        // append ORDER BY if requested
        if (orderByField != null) {
            if (!orderByField.getDeclaringClass().equals(clazz)) {
                throw new IllegalArgumentException(
                        "Order‐by field " + orderByField.getName() +
                                " not from " + clazz.getName());
            }
            String orderCol = fieldToColumnName.get(orderByField);
            if (orderCol == null) {
                throw new IllegalArgumentException(
                        "Missing order‐by field " + orderByField.getName());
            }
            sql.append(" ORDER BY ")
                    .append(orderCol)
                    .append(ascending ? " ASC" : " DESC");
        }

        return sql.toString();
    }

    public void prepareInsertStatement(PreparedStatement ps, T entity) throws SQLException {
        try {
            int i = 1;
            for (Field field : fieldToColumnName.keySet()) {
                Object value = field.get(entity);
                if (value instanceof Map) { // If map, encode it
                    String json = objectMapper.writeValueAsString(value);
                    ps.setObject(i++, json, java.sql.Types.OTHER); // .OTHER for PostgreSQL JSONB
                } else {
                    ps.setObject(i++, value);
                }
            }
        } catch (IllegalAccessException | JsonProcessingException e) {
            throw new RuntimeException("Failed to access or serialize field", e);
        }
    }

    public void prepareUpdateStatement(PreparedStatement ps, T entity) throws SQLException {
        try {
            int i = 1;
            for (Field field : fieldToColumnName.keySet()) {
                if (!field.equals(primaryKeyField)) {
                    Object value = field.get(entity);
                    if (value instanceof Map) { // If map, encode it
                        String json = objectMapper.writeValueAsString(value);
                        ps.setObject(i++, json, java.sql.Types.OTHER);
                    } else {
                        ps.setObject(i++, value);
                    }
                }
            }
            ps.setObject(i, primaryKeyField.get(entity));
        } catch (IllegalAccessException | JsonProcessingException e) {
            throw new RuntimeException("Failed to access or serialize field", e);
        }
    }

    public T buildFromTableWildcardQuery(ResultSet rs) throws SQLException {
        try {
            // 1) Find the right constructor
            Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(c -> c.isAnnotationPresent(TableConstructor.class))
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("No @TableConstructor on " + clazz.getName()));
            constructor.setAccessible(true);

            // 2) Match up fields ↔ constructor parameters
            Field[] fields     = clazz.getDeclaredFields();
            Parameter[] params = constructor.getParameters();
            if (params.length != fields.length) {
                throw new RuntimeException(
                        "Constructor parameter count (" + params.length +
                                ") does not match field count (" + fields.length + ")");
            }

            // 3) Read each column into an argument array
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Field field      = fields[i];
                String column    = fieldToColumnName.get(field);
                Class<?> type    = field.getType();

                if (Map.class.isAssignableFrom(type)) {
                    // JSON→Map column
                    String json = rs.getString(column);
                    if (json == null) {
                        args[i] = Collections.emptyMap();
                    } else {
                        args[i] = objectMapper.readValue(
                                json,
                                new TypeReference<Map<String,Object>>(){}
                        );
                    }
                } else {
                    // Simple column → Java type
                    args[i] = rs.getObject(column, type);
                }
            }

            // 4) Invoke the constructor with all the args
            return clazz.cast(constructor.newInstance(args));

        } catch (SQLException e) {
            throw e;  // pass SQL exceptions straight through
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create instance of " + clazz.getName(), e);
        }
    }


    @SuppressWarnings("unchecked")
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

