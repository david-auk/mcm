package com.mcm.backend.app.api.utils.annotations;

import com.mcm.backend.app.database.core.components.tables.TableEntity;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    Class<? extends TableEntity> value(); // User.class, Admin.class, etc.
}

