package com.mcm.backend.app.database.core.components.tables.timestaped;

import com.mcm.backend.app.database.core.components.tables.Table;

public abstract class TimestampedTable<T, K> extends Table<T, K> {
    protected final String timestampColumnName;

    protected TimestampedTable(String tableName, String primaryKeyColumnName, Class<K> primaryKeyDataType, String timestampColumnName, String addQuery, String updateQuery) {
        super(tableName, primaryKeyColumnName, primaryKeyDataType, addQuery, updateQuery);
        this.timestampColumnName = timestampColumnName;
    }

    public String getTimestampColumnName() {
        return timestampColumnName;
    }
}
