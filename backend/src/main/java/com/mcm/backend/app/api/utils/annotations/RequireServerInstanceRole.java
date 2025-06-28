package com.mcm.backend.app.api.utils.annotations;

import com.mcm.backend.app.database.models.roles.Role;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// TODO Add annotations
public @interface RequireServerInstanceRole {
    Role value();
}
