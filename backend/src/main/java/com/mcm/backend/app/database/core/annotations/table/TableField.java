package com.mcm.backend.app.database.core.annotations.table;

import java.lang.annotation.*;

// TODO Write JAVADOC
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableField {
    String name() default "";            // Optional override for column name

    /**
     * Required: the field's data type. Must be a non-primitive class
     * (use wrapper types like {@code Boolean.class} instead of {@code boolean.class}).
     */
    Class<?> type();
}
