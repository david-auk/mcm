package com.mcm.backend.app.database.core.components.daos;

import java.util.List;

public interface DAOInterface<T, K> extends AutoCloseable{
    boolean exists(T entity);
    boolean existsByPrimaryKey(K primaryKey);
    void add(T entity);
    //T create(T entity);
    void update(T entity);
    void delete(K primaryKey);
    T get(K primaryKey);
    List<T> getAll();
}
