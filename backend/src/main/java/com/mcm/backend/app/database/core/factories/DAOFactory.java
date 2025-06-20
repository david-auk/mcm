package com.mcm.backend.app.database.core.factories;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.tables.Table;
import com.mcm.backend.app.database.core.components.tables.TableEntity;

public class DAOFactory {

    public static <T extends TableEntity, K> DAO<T, K> createDAO(Class<T> clazz) {
        Table<T, K> table = TableRegistry.getTable(clazz);
        return new DAO<>(table);
    }
}

