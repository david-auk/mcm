package com.mcm.backend.database.core.factories;

import com.mcm.backend.database.core.components.daos.DAO;
import com.mcm.backend.database.core.components.daos.DAOInterface;

public class DAOFactory {

    public static <T, K> DAOInterface<T, K> createDAO(Class<T> clazz) {
        TableFactory<T, K> table = new TableFactory<>(clazz);
        return new DAO<>(table);
    }
}

