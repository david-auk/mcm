package com.mcm.backend.app.database.core.components.tables;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.lang.reflect.Field;
import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableColumn;
import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.annotations.table.ForeignKey;
import com.mcm.backend.app.database.core.components.daos.DAO;
import com.mcm.backend.app.api.utils.annotations.ValidatedBody;

/**
 * Marker interface for classes that can be used with {@link DAO} and {@link ValidatedBody}.
 * <p>
 * Classes should have a constructor annotated with {@link TableConstructor},
 * and use {@link TableName}, {@link PrimaryKey}, {@link TableColumn}, etc.
 */
public interface TableEntity {
    /**
     * Validates that a class is a well-formed TableEntity.
     * @param clazz the entity class to validate
     */
    static void validateEntity(Class<?> clazz) {
        // Check for @TableName
        if (!clazz.isAnnotationPresent(TableName.class)) {
            throw new IllegalStateException("Entity class " + clazz.getName() + " is missing @TableName");
        }

        // Find the primary key: field-level preferred (ignoring duplicated record accessors)
        Field pkField = getPkField(clazz);

        // Ensure a constructor is annotated @TableConstructor
        boolean hasTc = Arrays.stream(clazz.getDeclaredConstructors())
                .anyMatch(c -> c.isAnnotationPresent(TableConstructor.class));
        if (!hasTc) {
            throw new IllegalStateException("Entity class " + clazz.getName() +
                    " must have a constructor annotated @TableConstructor");
        }

        // If PK is a field, ensure that field has @TableColumn
        if (pkField != null && !pkField.isAnnotationPresent(TableColumn.class)) {
            throw new IllegalStateException("Primary key field " + pkField.getName() +
                    " in " + clazz.getName() + " must be annotated @TableColumn");
        }

        // Validate @ForeignKey usage: field type must implement TableEntity
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(ForeignKey.class)) {
                if (!TableEntity.class.isAssignableFrom(f.getType())) {
                    throw new IllegalStateException("Field " + f.getName() +
                            " in " + clazz.getName() +
                            " annotated @ForeignKey must have a type implementing TableEntity");
                }
            }

            // Ensure all @TableColumn fields are non-primitive types
            if (f.isAnnotationPresent(TableColumn.class)) {
                if (f.getType().isPrimitive()) {
                    throw new IllegalStateException("Field " + f.getName() +
                            " in " + clazz.getName() +
                            " annotated @TableColumn must not be a primitive type");
                }
            }
        }
    }

    private static Field getPkField(Class<?> clazz) {
        Field pkField = null;
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(PrimaryKey.class)) {
                if (pkField != null) {
                    throw new IllegalStateException("Entity class " + clazz.getName() +
                            " must have exactly one @PrimaryKey annotation on a field");
                }
                pkField = f;
            }
        }
        Method pkMethod = null;
        if (pkField == null) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(PrimaryKey.class)) {
                    if (pkMethod != null) {
                        throw new IllegalStateException("Entity class " + clazz.getName() +
                                " must have exactly one @PrimaryKey annotation on a method");
                    }
                    if (m.getParameterCount() != 0) {
                        throw new IllegalStateException("Primary key method " +
                                m.getName() + " in " + clazz.getName() +
                                " must have no parameters");
                    }
                    pkMethod = m;
                }
            }
            if (pkMethod == null) {
                throw new IllegalStateException("Entity class " + clazz.getName() +
                        " must have exactly one @PrimaryKey annotation");
            }
        }
        return pkField;
    }
}