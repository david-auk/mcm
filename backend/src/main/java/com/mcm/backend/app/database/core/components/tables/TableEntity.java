package com.mcm.backend.app.database.core.components.tables;

import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableField;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;

/**
 * Marker interface for classes that can be used with {@link DAO} and {@link ValidatedBody}.
 * <p>
 * Classes should have a constructor annotated with {@link TableConstructor},
 * and use {@link TableName}, {@link PrimaryKey}, {@link TableField}, etc.
 */
public interface TableEntity {
}