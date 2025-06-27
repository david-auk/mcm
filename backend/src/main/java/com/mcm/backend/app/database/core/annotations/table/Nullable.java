
package com.mcm.backend.app.database.core.annotations.table;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.mcm.backend.app.api.utils.components.ValidatedBodyResolver;


/**
 * Indicates that a field is automatically generated (for example, by the database or framework),
 * such as primary keys or timestamps.
 * <p>
 * Fields annotated with {@link Nullable} will be skipped if empty during request body binding
 * by {@link ValidatedBodyResolver},
 * and will always be passed as {@code null} to the entity constructor.
 * </p>
 *
 * @see com.mcm.backend.app.api.utils.components.ValidatedBodyResolver
 * @see com.mcm.backend.app.database.core.annotations.table.PrimaryKey
 * @see com.mcm.backend.app.database.core.annotations.table.TableField
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Nullable {
}