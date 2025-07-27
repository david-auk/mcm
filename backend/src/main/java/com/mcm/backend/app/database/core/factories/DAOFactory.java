package com.mcm.backend.app.database.core.factories;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.tables.Table;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

import java.sql.Connection;

public class DAOFactory {

    /**
     * Factory method to automatically create new DAO's relying on the DAO to open a new connection.
     * @param clazz The class extending TableEntity that can be converted to a Table and DAO
     * @return An initialized DAO
     * @param <T> TableEntity extending class type
     * @param <K> The PrimaryKey class type
     */
    public static <T extends TableEntity, K> DAO<T, K> createDAO(Class<T> clazz) {
        Table<T, K> table = TableRegistry.getTable(clazz);
        return new DAO<>(table);
    }

    /**
     * Factory method to automatically create new DAO's reusing an existing open connection
     * @param connection The open connection object that will be reused for the DAO
     * @param clazz The class extending TableEntity that can be converted to a Table and DAO
     * @return An initialized DAO
     * @param <T> TableEntity extending class type
     * @param <K> The PrimaryKey class type
     */
    public static <T extends TableEntity, K> DAO<T, K> createDAO(Connection connection, Class<T> clazz) {
        Table<T, K> table = TableRegistry.getTable(clazz);
        return new DAO<>(connection, table);
    }
}

