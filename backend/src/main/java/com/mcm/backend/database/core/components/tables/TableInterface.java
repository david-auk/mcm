package com.mcm.backend.database.core.components.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TableInterface<T, K> {
    void prepareAddStatement(PreparedStatement unPreparedStatement, T entity) throws SQLException;
    void prepareUpdateStatement(PreparedStatement unPreparedStatement, T entity) throws SQLException;
    T buildFromTableWildcardQuery(ResultSet resultSet) throws SQLException;
    K getPrimaryKey(T entity);
}
