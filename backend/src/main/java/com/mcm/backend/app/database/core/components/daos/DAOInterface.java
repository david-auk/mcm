package com.mcm.backend.app.database.core.components.daos;

import java.lang.reflect.Field;
import java.util.List;

public interface DAOInterface<T, K> extends AutoCloseable{
    boolean exists(T entity);
    boolean existsByPrimaryKey(K primaryKey);
    void add(T entity);
    void update(T entity);
    void delete(K primaryKey);
    T get(K primaryKey);
    <D> List<T> get(Field whereField, D isData, boolean wildcardQuery);
    List<T> getAll();
}
