package com.mcm.backend.app.database.core.annotations.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the primary‐key provider for this entity.
 * <p>
 * Can be placed on:
 * <ul>
 *   <li>a field, in which case that field’s value is used directly as the PK</li>
 *   <li>a zero‐argument method, in which case its return value is used as the PK</li>
 * </ul>
 * <p>
 * The field or method must uniquely identify a row in the table.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {
}