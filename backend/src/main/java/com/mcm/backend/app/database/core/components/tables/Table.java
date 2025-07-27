package com.mcm.backend.app.database.core.components.tables;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcm.backend.app.database.core.annotations.table.ForeignKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableColumn;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.querying.FilterCriterion;
import com.mcm.backend.app.database.core.factories.DAOFactory;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Table<T, K> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Class<T> clazz;
    private final AccessibleObject primaryKeyMember;
    private final Class<K> primaryKeyDataType;
    private final Map<Field, String> fieldToColumnName;
    private final List<Field> nonPkFields;
    private final List<Field> pkFields;

    protected final String tableName;
    protected final String insertQuery;
    protected final String updateQuery;

    @SuppressWarnings("unchecked")
    public Table(Class<T> clazz) {
        this.clazz = clazz;
        this.tableName = TableUtils.getTableName(clazz);

        // map fields → column names
        this.fieldToColumnName = TableUtils.mapFieldToColumnNames(clazz);

        // pick up the @PrimaryKey (field or zero-arg method)
        this.primaryKeyMember = TableUtils.getPrimaryKeyMember(clazz);
        this.primaryKeyDataType   = (Class<K>) TableUtils.getPrimaryKeyType(clazz);

        // figure out which Fields back the PK for SQL binding
        if (primaryKeyMember instanceof Field) {
            this.pkFields = Collections.singletonList((Field) primaryKeyMember);
        } else {
            // composite: every @TableField column is part of the PK
            this.pkFields = new ArrayList<>(fieldToColumnName.keySet());
        }

        // fields to SET in UPDATE
        this.nonPkFields = fieldToColumnName.keySet().stream()
                .filter(f -> !pkFields.contains(f))
                .collect(Collectors.toList());

        // build SQL
        this.insertQuery = TableUtils.buildInsertQuery(
                tableName,
                fieldToColumnName.values()
        );
        this.updateQuery = TableUtils.buildUpdateQuery(clazz);
    }

    // ——————————————————————————————————————————————————————————
    //  Query builder
    // ——————————————————————————————————————————————————————————

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
        // unchanged from your original
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
                        "Order-by field " + orderByField.getName() +
                                " not from " + clazz.getName());
            }
            String orderCol = fieldToColumnName.get(orderByField);
            if (orderCol == null) {
                throw new IllegalArgumentException(
                        "Missing order-by field " + orderByField.getName());
            }
            sql.append(" ORDER BY ")
                    .append(orderCol)
                    .append(ascending ? " ASC" : " DESC");
        }

        return sql.toString();
    }

    public void prepareInsertStatement(PreparedStatement ps, T entity) throws SQLException {
        try {
            int idx = 1;
            for (Field field : fieldToColumnName.keySet()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(ForeignKey.class)) {
                    Object refObj = field.get(entity);
                    Object fkValue = (refObj == null) ? null : TableUtils.getPrimaryKeyValue(refObj);
                    ps.setObject(idx++, fkValue);
                } else {
                    Object value = field.get(entity);
                    if (value instanceof Map) {
                        String json = objectMapper.writeValueAsString(value);
                        ps.setObject(idx++, json, Types.OTHER);
                    } else {
                        ps.setObject(idx++, value);
                    }
                }
            }
        } catch (IllegalAccessException | JsonProcessingException e) {
            throw new RuntimeException("Failed to access or serialize field", e);
        }
    }

    public void prepareUpdateStatement(PreparedStatement ps, T entity) throws SQLException {
        try {
            int idx = 1;
            // 1) SET clauses
            for (Field field : nonPkFields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(ForeignKey.class)) {
                    Object refObj = field.get(entity);
                    Object fkValue = (refObj == null) ? null : TableUtils.getPrimaryKeyValue(refObj);
                    ps.setObject(idx++, fkValue);
                } else {
                    Object value = field.get(entity);
                    if (value instanceof Map) {
                        String json = objectMapper.writeValueAsString(value);
                        ps.setObject(idx++, json, Types.OTHER);
                    } else {
                        ps.setObject(idx++, value);
                    }
                }
            }
            // 2) WHERE clauses (all PK fields, in declaration order)
            for (Field pkField : pkFields) {
                pkField.setAccessible(true);
                ps.setObject(idx++, pkField.get(entity));
            }
        } catch (IllegalAccessException | JsonProcessingException e) {
            throw new RuntimeException("Failed to access or serialize field", e);
        }
    }

    // ——————————————————————————————————————————————————————————
    //  ResultSet → entity
    // ——————————————————————————————————————————————————————————

    public T buildFromTableWildcardQuery(Connection connection, ResultSet rs) throws SQLException {
        try {
            // changed line to only include @TableField fields
            Field[] fields = Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(TableColumn.class))
                    .toArray(Field[]::new);
            Constructor<?> constructor = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(c -> c.isAnnotationPresent(TableConstructor.class))
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("No @TableConstructor on " + clazz.getName()));
            constructor.setAccessible(true);

            // Match up fields ↔ constructor parameters
            Parameter[] params = constructor.getParameters();
            if (params.length != fields.length) {
                throw new RuntimeException(
                        "Constructor parameter count (" + params.length +
                                ") does not match field count (" + fields.length + ")");
            }

            // Read each column into an argument array
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Field field   = fields[i];
                String column = fieldToColumnName.get(field);
                Class<?> type = field.getType();

                if (field.isAnnotationPresent(ForeignKey.class)) {
                    // Ensure the field type implements TableEntity
                    if (!TableEntity.class.isAssignableFrom(type)) {
                        throw new IllegalArgumentException("Foreign key Class does not extend TableEntity");
                    }
                    @SuppressWarnings("unchecked")
                    Class<? extends TableEntity> refClass = (Class<? extends TableEntity>) type;
                    // Determine primary key type for fetching the raw ID
                    Class<?> pkType = TableUtils.getPrimaryKeyType(refClass);
                    Object fkId = rs.getObject(column, pkType);

                    args[i] = (fkId == null) ? null : loadReference(connection, refClass, fkId);
                } else {
                    if (Map.class.isAssignableFrom(type)) {
                        // JSON→Map column
                        String json = rs.getString(column);
                        args[i] = (json == null)
                                ? Collections.emptyMap()
                                : objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
                        });
                    } else {
                        args[i] = rs.getObject(column, type);
                    }
                }
            }

            // Invoke the constructor with all the args
            return clazz.cast(constructor.newInstance(args));

        } catch (Exception e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }


    /**
     * Returns the raw PK value
     */
    @SuppressWarnings("unchecked")
    public K getPrimaryKey(T entity) {
        return (K) TableUtils.getPrimaryKeyValue(entity);
    }

    /**
     * If you need the actual column name for a single‐field PK.
     * Throws if you’re using a composite key.
     */
    public String getPrimaryKeyColumnName() {
        if (primaryKeyMember instanceof Field) {
            return fieldToColumnName.get(primaryKeyMember);
        }
        throw new UnsupportedOperationException(
                "Composite primary key — use getPrimaryKeyColumnNames()");
    }

    // ——————————————————————————————————————————————————————————
    //  getters & toString()
    // ——————————————————————————————————————————————————————————

    public String getInsertQuery()     { return insertQuery; }
    public String getUpdateQuery()     { return updateQuery; }
    public String getTableName()       { return tableName; }

    /**
     * Generic loader for referenced entities.
     *
     * @param refClass the entity class
     * @param key      the primary key value
     * @param <R>      the entity type (must extend TableEntity)
     * @param <P>      the primary key type
     * @return the loaded entity or null if not found
     */
    private <R extends TableEntity, P> R loadReference(Connection connection, Class<R> refClass, P key) {
        try (DAO<R, P> dao = DAOFactory.createDAO(connection, refClass)) {
            return dao.get(key);
        }
    }

    public String getColumnName(Field uniqueField) {
        String columnName = fieldToColumnName.get(uniqueField);
        if (columnName == null) {
            throw new IllegalArgumentException(
                    "Field '" + uniqueField.getName() + "' is not a column in table '" + tableName + "'"
            );
        }
        return columnName;
    }

    @Override
    public String toString() {
        return "Table[" + clazz.getSimpleName() + "]:\n" +
                "  Table name: '" + tableName + "'\n" +
                "  PK member: " + primaryKeyMember + " (" +
                primaryKeyDataType.getSimpleName() + ")\n" +
                "  Columns: " + fieldToColumnName.values() + "\n" +
                "  INSERT: " + insertQuery + "\n" +
                "  UPDATE: " + updateQuery + "\n";
    }
}