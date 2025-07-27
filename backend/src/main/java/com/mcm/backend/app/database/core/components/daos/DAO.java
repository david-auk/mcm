package com.mcm.backend.app.database.core.components.daos;

import com.mcm.backend.app.database.core.annotations.table.UniqueColumn;
import com.mcm.backend.app.database.core.components.Database;
import com.mcm.backend.app.database.core.components.daos.querying.FilterCriterion;
import com.mcm.backend.app.database.core.components.daos.querying.QueryBuilder;
import com.mcm.backend.app.database.core.components.tables.Table;
import com.mcm.backend.app.database.core.components.tables.TableEntity;
import com.mcm.backend.app.database.core.components.tables.TableUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO<T, K> implements AutoCloseable {

    protected final Connection connection;
    private final Boolean connectionOpened;
    private final Table<T, K> table;

    /**
     * Constructs a DAO with a new database connection for the specified table.
     *
     * @param table the table metadata for which this DAO operates
     */
    public DAO(Table<T, K> table) {
        this.table = table;
        this.connection = Database.getConnection();
        this.connectionOpened = true;
    }

    /**
     * Constructs a DAO reusing an existing database connection for the specified table.
     *
     * @param connection the existing SQL connection
     * @param table the table metadata for which this DAO operates
     */
    public DAO(Connection connection, Table<T, K> table) {
        this.connection = connection;
        this.connectionOpened = false;
        this.table = table;

        // TODO check if validation computing tax is worth it
//        try {
//            if (connection != null && !connection.isClosed() && connection.isValid(0)) {
//                this.connection = connection;
//            } else {
//                throw new IllegalArgumentException("Invalid connection");
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * Checks if a record exists in the table with the given primary key.
     *
     * @param primaryKey the primary key value to check
     * @return true if a record with the primary key exists; false otherwise
     */
    public boolean existsByPrimaryKey(K primaryKey) {
        if (primaryKey == null) {
            return false;
        }
        return queryUniqueFieldExists(table.getPrimaryKeyColumnName(), primaryKey);
    }

    /**
     * Checks if a record exists in the table with the specified unique field value.
     *
     * @param uniqueField the field annotated with {@link UniqueColumn} to check
     * @param isData the value to match against the unique field
     * @param <D> the type of the field value
     * @return true if a matching record exists; false otherwise
     * @throws RuntimeException if the field is not annotated with {@link UniqueColumn}
     */
    public <D> boolean existsByUniqueField(Field uniqueField, D isData) {
        if (!uniqueField.isAnnotationPresent(UniqueColumn.class)) {
            throw new RuntimeException("Field " + uniqueField.getName() + " is not annotated with @UniqueColumn");
        }
        return queryUniqueFieldExists(table.getColumnName(uniqueField), isData);
    }

    private <D> boolean queryUniqueFieldExists(String columnName, D isData) {
        String query = String.format(
            "SELECT 1 FROM %s WHERE %s = ? LIMIT 1",
            table.getTableName(), columnName
        );

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            Object bindValue = isData;
            if (isData instanceof TableEntity) {
                bindValue = TableUtils.getPrimaryKeyValue(isData);
            }
            ps.setObject(1, bindValue);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a record exists for the given entity based on its primary key.
     *
     * @param entity the entity to check existence for
     * @return true if the entity exists; false otherwise
     */
    public boolean exists(T entity) {
        if (entity == null) {
            return false;
        }
        return existsByPrimaryKey(table.getPrimaryKey(entity));
    }

    /**
     * Inserts the specified entity into the table if it does not already exist.
     *
     * @param entity the entity to insert
     */
    public void add(T entity) {
        if (!exists(entity)) {
            try {
                PreparedStatement addStatement = connection.prepareStatement(table.getInsertQuery());
                table.prepareInsertStatement(addStatement, entity);
                addStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Updates the specified entity in the table.
     *
     * @param entity the entity to update
     * @throws RuntimeException if the entity does not exist or a SQL error occurs
     */
    public void update(T entity) {
        if (!exists(entity)) {
            throw new RuntimeException("Entity does not exist.");
        }
        try {
            PreparedStatement updateStatement = connection.prepareStatement(table.getUpdateQuery());
            table.prepareUpdateStatement(updateStatement, entity);
            updateStatement.executeUpdate();
            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("No rows affected");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves an entity by its primary key.
     *
     * @param primaryKey the primary key value of the entity to retrieve
     * @return the entity if found; null otherwise
     */
    public T get(K primaryKey) {
        T entity = null;

        String query = "SELECT * FROM %s WHERE %s = ?";
        query = String.format(query, table.getTableName(), table.getPrimaryKeyColumnName());

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            // Unwrap TableEntity keys to their actual PK value if necessary
            Object bindValue = primaryKey;
            if (primaryKey instanceof TableEntity) { // If primaryKey is a foreign object
                // Get and use the foreign object's pk (this.pk = foreignObject.pk)
                bindValue = TableUtils.getPrimaryKeyValue(primaryKey);
            }
            preparedStatement.setObject(1, bindValue);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                entity = table.buildFromTableWildcardQuery(connection, resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    /**
     * Retrieve entities by multiple filter criteria and optional ordering.
     *
     * @param filters      the list of filter criteria (each with its own wildcard flag);
     *                     any criterion whose value is null will be skipped
     * @param orderByField the entity Field to sort by (or null for no ordering)
     * @param ascending    true for ASC, false for DESC
     * @return a List of matching entities
     * @throws RuntimeException         if a SQL error occurs
     * @throws IllegalArgumentException if any Field is invalid for this entity
     */
    public List<T> get(
            List<FilterCriterion<?>> filters,
            Field orderByField,
            boolean ascending
    ) {
        // 1) build the SQL
        String sql = table.buildGetQuery(filters, orderByField, ascending);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // 2) bind parameters in the same order
            int idx = 1;
            for (FilterCriterion<?> criterion : filters) {
                Object value = criterion.getValue();
                if (value != null) {
                    ps.setObject(idx++, value);
                }
            }

            // 3) execute and map to entities
            try (ResultSet rs = ps.executeQuery()) {
                return getEntities(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query: " + sql, e);
        }
    }

    /**
     * Get method that builds a where query
     *
     * @param whereField The field you are sorting for
     * @param isData     The data you want to match
     * @return Entities from query
     */
    public <D> List<T> get(Field whereField, D isData) {
        return new QueryBuilder<>(this)
                .where(whereField, isData)
                .get();
    }

    /**
     * Retrieves a unique entity by its unique field value.
     *
     * @param uniqueField the field annotated with @UniqueColumn to query
     * @param isData the value to match
     * @param <D> the type of the field value
     * @return the unique entity if found; null if no match
     * @throws RuntimeException if the field is not annotated with @UniqueColumn
     * @throws IllegalStateException if multiple results are found
     */
    public <D> T getUnique(Field uniqueField, D isData) {
        if (!uniqueField.isAnnotationPresent(UniqueColumn.class)) {
            throw new RuntimeException("Field " + uniqueField.getName() + " is not annotated with @UniqueField");
        }
        List<T> results = get(uniqueField, isData);
        if (results.size() > 1) {
            throw new IllegalStateException("Multiple results found for unique field: " + uniqueField.getName() + " with value: " + isData.toString());
        } else if (results.isEmpty()) {
            return null;
        } else {
            return results.getFirst();
        }
    }

    /**
     * Deletes the record with the specified primary key from the table.
     *
     * @param primaryKey the primary key value of the record to delete
     * @throws RuntimeException if the record does not exist or a SQL error occurs
     */
    public void delete(K primaryKey) {
        if (!existsByPrimaryKey(primaryKey)) {
            throw new RuntimeException("Record does not exist.");
        }

        String query = "DELETE FROM %s WHERE %s = ?";
        query = String.format(query, table.getTableName(), table.getPrimaryKeyColumnName());

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            preparedStatement.setObject(1, primaryKey);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves all entities from the table.
     *
     * @return a list of all entities
     */
    public List<T> getAll() {
        String query = "SELECT * FROM %s";
        query = String.format(query, table.getTableName());
        List<T> entities;
        try {
            Statement statement = connection.createStatement();
            entities = getEntities(statement.executeQuery(query));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return entities;
    }

    /**
     * Converts the provided ResultSet into a list of entities.
     *
     * @param resultSet the ResultSet from a wildcard query
     * @return a list of entities built from the ResultSet
     * @throws SQLException if a database access error occurs
     */
    protected List<T> getEntities(ResultSet resultSet) throws SQLException {
        List<T> entities = new ArrayList<>();
        while (resultSet.next()) {
            T entity = table.buildFromTableWildcardQuery(connection, resultSet);
            entities.add(entity);
        }
        return entities;
    }

    /**
     * Closes the underlying database connection.
     */
    public void close() {
        // Only close the connection if it was opened by the constructor
        if (connectionOpened) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
