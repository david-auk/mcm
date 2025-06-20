package com.mcm.backend.app.database.core.annotations.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking primary keys
 * <p>
 * Field name must be the same as the column name.
 */


// TODO Write (Better) JAVADOC
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {
    Class<?> value(); // to specify the type of the primary key
}