package com.mcm.backend.app.database.core.factories;

import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.database.core.components.daos.DAOInterface;

public class DAOFactory {

    public static <T, K> DAO<T, K> createDAO(Class<T> clazz) {
        TableFactory<T, K> table = new TableFactory<>(clazz);
        return new DAO<>(table);
    }
}

