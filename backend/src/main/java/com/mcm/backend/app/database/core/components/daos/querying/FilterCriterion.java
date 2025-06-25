package com.mcm.backend.app.database.core.components.daos.querying;

import java.lang.reflect.Field;

/**
 * A single filter condition: “field [=|LIKE] value”.
 */
public class FilterCriterion<T> {
    private final Field field;
    private final T value;
    private final boolean wildcard;

    public FilterCriterion(Field field, T value, boolean wildcard) {
        this.field     = field;
        this.value     = value;
        this.wildcard  = wildcard;
    }

    public Field getField()        { return field; }
    public T     getValue()        { return value; }
    public boolean isWildcard()    { return wildcard; }
}
