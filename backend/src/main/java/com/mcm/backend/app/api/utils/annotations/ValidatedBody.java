package com.mcm.backend.app.api.utils.annotations;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidatedBody {

    /**
     * Target class to instantiate from the request body.
     * Must implement {@link TableEntity}.
     */
    Class<? extends TableEntity> value();
}