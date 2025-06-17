package com.mcm.backend.app.database.core.annotations.table;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableField {
    String name() default "";            // Optional override for column name
    Class<?> type();                     // Required: the field's data type
}
