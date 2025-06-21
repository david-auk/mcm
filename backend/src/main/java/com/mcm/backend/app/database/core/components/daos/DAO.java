package com.mcm.backend.app.database.core.components.daos;

import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.UniqueField;
import com.mcm.backend.app.database.core.components.Database;
import com.mcm.backend.app.database.core.components.tables.Table;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO<T, K> implements DAOInterface<T, K> {

    protected final Connection connection;
    private final Table<T, K> table;

    // Constructor to enforce initialization
    public DAO(Table<T, K> table) {
        this.table = table;
        this.connection = Database.getConnection();
    }

    @Override
    public boolean existsByPrimaryKey(K primaryKey) {
        return primaryKey != null && get(primaryKey) != null;
    }

    @Override
    public boolean exists(T entity) {
        if (entity == null){
            return false;
        }
        return existsByPrimaryKey(table.getPrimaryKey(entity));
    }

    @Override
    public void add(T entity) {
        if (!exists(entity)){
            try {
                PreparedStatement addStatement = connection.prepareStatement(table.getInsertQuery());
                table.prepareInsertStatement(addStatement, entity);
                addStatement.executeUpdate();
            } catch (SQLException e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void update(T entity) {
        if (!exists(entity)){
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

    @Override
    public T get(K primaryKey) {
        T entity = null;

        String query = "SELECT * FROM %s WHERE %s = ?";
        query = String.format(query, table.getTableName(), table.getPrimaryKeyColumnName());

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, primaryKey);
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
     * Get method that builds a where query
     * @param whereField The field you are sorting for
     * @param isData The data you want to match
     * @param wildcardQuery Boolean option to choose if you want to use LIKE operator
     * @return Entities from query
     */
    @Override
    public <D> List<T> get(Field whereField, D isData, boolean wildcardQuery) {
        String query = table.buildGetQuery(whereField, isData, wildcardQuery);
        List<T> entities;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, isData);
            entities = getEntities(preparedStatement.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return entities;
    }

    /**
     * Get method that builds a where query
     * @param whereField The field you are sorting for
     * @param isData The data you want to match
     * @return Entities from query
     */
    public <D> List<T> get(Field whereField, D isData) {
        return get(whereField, isData, false);
    }

    public <D> T getUnique(Field uniqueField, D isData) {
        if (!uniqueField.isAnnotationPresent(UniqueField.class)) {
            throw new RuntimeException("Field " + uniqueField.getName() + " is not annotated with @UniqueField");
        }
        List<T> matches = get(uniqueField, isData, false);
        if (matches.isEmpty()) {
            return null;
        }
        return matches.getFirst();
    }

    @Override
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

    @Override
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

    @Override
    public void close() {
        Database.closeConnection(connection);
    }
}
