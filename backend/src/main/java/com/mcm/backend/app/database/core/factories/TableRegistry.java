package com.mcm.backend.app.database.core.factories;
import com.mcm.backend.app.database.core.components.tables.Table;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Table Registry (Singleton)
 */
public class TableRegistry {

    private static final Map<Class<?>, Table<?, ?>> cache = new ConcurrentHashMap<>();

    /**
     * When a table instance is created it is not expected that a table changes for a specific instance during runtime.
     * @param clazz The class-type of the AutoTableEntity
     * @return A cased or newly created table instance of clazz
     */
    @SuppressWarnings("unchecked")
    public static <T, K> Table<T, K> getTable(Class<T> clazz) {
        return (Table<T, K>) cache.computeIfAbsent(clazz, Table::new);
    }

    private TableRegistry() {
        // prevent instantiation
    }
}
