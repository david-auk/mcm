package com.mcm.backend.app.database.core.factories;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.tables.Table;

public class DAOFactory {

    public static <T, K> DAO<T, K> createDAO(Class<T> clazz) {
        Table<T, K> table = new Table<>(clazz);
        return new DAO<>(table);
    }
}

