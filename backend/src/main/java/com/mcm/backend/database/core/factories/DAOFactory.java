package com.mcm.backend.database.core.factories;

import com.mcm.backend.database.core.components.DAO;
import com.mcm.backend.database.core.components.DAOInterface;

public class DAOFactory {

    public static <T, K> DAOInterface<T, K> createDAO(Class<T> clazz) {

        // Could this replace the "K" definition so i only have to change the model?
        //Class<?> privateKeyClass = ReflectiveTable.getPrimaryKeyField(clazz).getClass();

        ReflectiveTable<T, K> table = new ReflectiveTable<>(clazz);
        return new DAO<>(table);
    }
}

