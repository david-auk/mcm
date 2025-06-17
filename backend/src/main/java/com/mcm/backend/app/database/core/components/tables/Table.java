package com.mcm.backend.app.database.core.components.tables;

public abstract class Table<T, K> implements TableInterface<T, K> {
    protected final String tableName;
    protected final String primaryKeyColumnName;
    protected final Class<K> primaryKeyDataType;
    private final String addQuery;
    private final String updateQuery;

    protected Table(String tableName, String primaryKeyColumnName, Class<K> primaryKeyDataType, String addQuery, String updateQuery) {
        this.tableName = tableName;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.primaryKeyDataType = primaryKeyDataType;
        this.addQuery = addQuery;
        this.updateQuery = updateQuery;
    }

    public String getTableName() {
        return tableName;
    }

    public String getPrimaryKeyColumnName() {
        return primaryKeyColumnName;
    }

    public Class<K> getPrimaryKeyDataType() {
        return primaryKeyDataType;
    }

    public String getAddQuery() {
        return addQuery;
    }

    public String getUpdateQuery() {
        return updateQuery;
    }
}
