package com.mcm.backend.app.database.core.components.tables;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TableInterface<T, K> {
    <D> String buildGetQuery(Field field, D data, boolean wildcardQuery);
    void prepareInsertStatement(PreparedStatement unPreparedStatement, T entity) throws SQLException;
    void prepareUpdateStatement(PreparedStatement unPreparedStatement, T entity) throws SQLException;
    T buildFromTableWildcardQuery(ResultSet resultSet) throws SQLException;
    K getPrimaryKey(T entity);
}
