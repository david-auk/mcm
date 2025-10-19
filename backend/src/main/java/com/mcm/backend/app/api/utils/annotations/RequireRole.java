package com.mcm.backend.app.api.utils.annotations;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    Class<? extends TableEntity> value(); // User.class, Admin.class, etc.
}

