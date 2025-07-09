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
    private final Table<T, K> table;

    // Constructor to enforce initialization
    public DAO(Table<T, K> table) {
        this.table = table;
        this.connection = Database.getConnection();
    }

    public boolean existsByPrimaryKey(K primaryKey) {
        return primaryKey != null && get(primaryKey) != null;
    }

    public <D> boolean existsByUniqueField(Field uniqueField, D isData) {
        if (!uniqueField.isAnnotationPresent(UniqueColumn.class)) {
            throw new RuntimeException("Field " + uniqueField.getName() + " is not annotated with @UniqueField");
        }
        List<T> matches = get(uniqueField, isData);
        return !matches.isEmpty();
    }

    public boolean exists(T entity) {
        if (entity == null) {
            return false;
        }
        return existsByPrimaryKey(table.getPrimaryKey(entity));
    }

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

    public T get(K primaryKey) {
        T entity = null;

        String query = "SELECT * FROM %s WHERE %s = ?";
        query = String.format(query, table.getTableName(), table.getPrimaryKeyColumnName());

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            // Unwrap TableEntity keys to their actual PK value if necessary
            Object bindValue = primaryKey;
            if (primaryKey instanceof TableEntity) { // If primaryKey is a foreign object
                // Get and use the foreign objects pk (this.pk = foreignObject.pk)
                bindValue = TableUtils.getPrimaryKeyValue(primaryKey);
            }
            preparedStatement.setObject(1, bindValue);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                entity = table.buildFromTableWildcardQuery(resultSet);
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
     * Helper method to convert a resultSet (list) into an entity list
     *
     * @param resultSet A resultSet from a wildcard query, so it can be built using the  buildFromTableWildcardQuery
     * @return A list of entities representing the query results
     * @throws SQLException Exception that is meant to be caught by implementing methods
     */
    protected List<T> getEntities(ResultSet resultSet) throws SQLException {
        List<T> entities = new ArrayList<>();
        while (resultSet.next()) {
            T entity = table.buildFromTableWildcardQuery(resultSet);
            entities.add(entity);
        }
        return entities;
    }

    public void close() {
        Database.closeConnection(connection);
    }
}
