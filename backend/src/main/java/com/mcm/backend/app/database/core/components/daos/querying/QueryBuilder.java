package com.mcm.backend.app.database.core.components.daos.querying;

import com.mcm.backend.app.database.core.components.daos.DAO;
import jakarta.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder<T, K> {
    private final DAO<T, K> dao;
    private final List<FilterCriterion<?>> filters = new ArrayList<>();
    @Nullable
    private Field orderByField;
    private boolean ascending = true;

    public QueryBuilder(DAO<T, K> dao) {
        this.dao = dao;
    }

    /**
     * Add an “= value” filter.
     */
    public <D> QueryBuilder<T, K> where(Field field, D value) {
        filters.add(new FilterCriterion<>(field, value, false));
        return this;
    }

    /**
     * Add a “LIKE value” filter.
     */
    public <D> QueryBuilder<T, K> whereLike(Field field, D value) {
        filters.add(new FilterCriterion<>(field, value, true));
        return this;
    }

    /**
     * Alias for where(...).
     */
    public <D> QueryBuilder<T, K> and(Field field, D value) {
        return where(field, value);
    }

    /**
     * Alias for whereLike(...).
     */
    public <D> QueryBuilder<T, K> andLike(Field field, D value) {
        return whereLike(field, value);
    }

    /**
     * Specify ORDER BY field.
     */
    public QueryBuilder<T, K> orderBy(Field field) {
        this.orderByField = field;
        return this;
    }

    /** Subsequent .get() will sort ascending. */
    public QueryBuilder<T, K> asc() {
        this.ascending = true;
        return this;
    }

    /** Subsequent .get() will sort descending. */
    public QueryBuilder<T, K> desc() {
        this.ascending = false;
        return this;
    }

    /**
     * Execute the query and return matched entities.
     */
    public List<T> get() {
        return dao.get(filters, orderByField, ascending);
    }

    public T getUnique() {
        List<T> results = dao.get(filters, orderByField, ascending);
        if (results.size() > 1) {
            throw new IllegalStateException("Multiple results found for query: " + filters);
        } else if (results.isEmpty()) {
            return null;
        } else {
            return results.getFirst();
        }
    }
}

