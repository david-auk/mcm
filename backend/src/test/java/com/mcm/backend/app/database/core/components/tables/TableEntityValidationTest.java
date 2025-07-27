package com.mcm.backend.app.database.core.components.tables;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.reflections.Reflections;

import java.util.Set;
import java.util.UUID;

import com.mcm.backend.app.database.core.annotations.table.TableName;
import com.mcm.backend.app.database.core.annotations.table.PrimaryKey;
import com.mcm.backend.app.database.core.annotations.table.TableConstructor;
import com.mcm.backend.app.database.core.annotations.table.TableColumn;
import com.mcm.backend.app.database.core.annotations.table.ForeignKey;

/**
 * Unit tests for TableEntity.validateEntity.
 *
 * <p>This test suite verifies that:
 * 1) All real entity classes are valid.
 * 2) Specific mis-annotation scenarios throw the expected exception.</p>
 */
class TableEntityValidationTest {

    /**
     * Test that all real entities under com.mcm.backend.app.database.models pass validation.
     */
    @Test
    void validateAllRealEntities() {
        Reflections reflections = new Reflections("com.mcm.backend.app.database.models");
        Set<Class<? extends TableEntity>> entities =
                reflections.getSubTypesOf(TableEntity.class);

        for (Class<? extends TableEntity> entity : entities) {
            Assertions.assertDoesNotThrow(
                    () -> TableEntity.validateEntity(entity),
                    () -> "Validation failed for " + entity.getName()
            );
        }
    }

    // 1) Missing @TableName entirely
    static class NoTableNameEntity implements TableEntity {
        @PrimaryKey @TableColumn
        private final UUID id;
        @TableConstructor
        public NoTableNameEntity(UUID id) { this.id = id; }
    }
    /**
     * Scenario 1: Entity missing @TableName should throw IllegalStateException.
     */
    @Test
    void missingTableNameShouldThrow() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> TableEntity.validateEntity(NoTableNameEntity.class),
                "Expected missing @TableName to fail"
        );
    }

    // 2) Missing any @PrimaryKey annotation
    @TableName("no_pk")
    static class NoPrimaryKeyEntity implements TableEntity {
        @TableColumn
        private final UUID id;
        @TableConstructor
        public NoPrimaryKeyEntity(UUID id) { this.id = id; }
    }
    /**
     * Scenario 2: Entity missing @PrimaryKey should throw IllegalStateException.
     */
    @Test
    void missingPrimaryKeyShouldThrow() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> TableEntity.validateEntity(NoPrimaryKeyEntity.class),
                "Expected missing @PrimaryKey to fail"
        );
    }

    // 3) Two @PrimaryKey on fields
    @TableName("two_field_pk")
    static class TwoFieldPkEntity implements TableEntity {
        @PrimaryKey @TableColumn private final UUID id1;
        @PrimaryKey @TableColumn private final UUID id2;
        @TableConstructor
        public TwoFieldPkEntity(UUID id1, UUID id2) {
            this.id1 = id1; this.id2 = id2;
        }
    }
    /**
     * Scenario 3: Entity with multiple @PrimaryKey fields should throw IllegalStateException.
     */
    @Test
    void multipleFieldPrimaryKeysShouldThrow() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> TableEntity.validateEntity(TwoFieldPkEntity.class),
                "Expected two @PrimaryKey on fields to fail"
        );
    }

    // 4) @PrimaryKey on a method with parameters
    @TableName("pk_on_method_with_params")
    static class BadMethodPkEntity implements TableEntity {
        @TableColumn private final UUID id;
        public BadMethodPkEntity(UUID id) { this.id = id; }
        @PrimaryKey
        public UUID getId(String prefix) { return id; }  // illegal: method has parameters
    }
    /**
     * Scenario 4: @PrimaryKey on a method with parameters should throw IllegalStateException.
     */
    @Test
    void primaryKeyMethodWithParamsShouldThrow() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> TableEntity.validateEntity(BadMethodPkEntity.class),
                "Expected @PrimaryKey method with parameters to fail"
        );
    }

    // 5) Two @PrimaryKey on methods
    @TableName("two_method_pk")
    static class TwoMethodPkEntity implements TableEntity {
        @TableColumn private final UUID id;
        @TableConstructor
        public TwoMethodPkEntity(UUID id) { this.id = id; }
        @PrimaryKey
        public UUID getId() { return this.id; }
        @PrimaryKey
        public UUID getAltId() { return this.id; }
    }
    /**
     * Scenario 5: Entity with multiple @PrimaryKey methods should throw IllegalStateException.
     */
    @Test
    void multipleMethodPrimaryKeysShouldThrow() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> TableEntity.validateEntity(TwoMethodPkEntity.class),
            "Expected multiple @PrimaryKey on methods to fail"
        );
    }

    // 6) Missing constructor annotated @TableConstructor
    @TableName("no_constructor")
    static class NoConstructorEntity implements TableEntity {
        @PrimaryKey @TableColumn private final UUID id;
        public NoConstructorEntity(UUID id) { this.id = id; }  // illegal: no @TableConstructor
    }
    /**
     * Scenario 6: Entity missing @TableConstructor should throw IllegalStateException.
     */
    @Test
    void missingTableConstructorShouldThrow() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> TableEntity.validateEntity(NoConstructorEntity.class),
                "Expected missing @TableConstructor to fail"
        );
    }

    // 7) @PrimaryKey field not annotated @TableColumn
    @TableName("pk_not_column")
    static class PkNotTableColumnEntity implements TableEntity {
        @PrimaryKey private final UUID id;             // illegal: missing @TableColumn
        @TableConstructor
        public PkNotTableColumnEntity(UUID id) { this.id = id; }
    }
    /**
     * Scenario 7: @PrimaryKey field without @TableColumn should throw IllegalStateException.
     */
    @Test
    void primaryKeyFieldWithoutTableColumnShouldThrow() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> TableEntity.validateEntity(PkNotTableColumnEntity.class),
                "Expected @PrimaryKey field without @TableColumn to fail"
        );
    }

    // ---------- Valid entity examples and tests ----------

    /**
     * Scenario V1: minimal valid entity (field-level @PrimaryKey) should pass validation.
     */
    @TableName("good_entity")
    static class GoodEntity implements TableEntity {
        @PrimaryKey @TableColumn private final UUID id;
        @TableConstructor
        public GoodEntity(UUID id) { this.id = id; }
    }
    @Test
    void validMinimalEntityShouldPass() {
        // No exception means validation passed
        Assertions.assertDoesNotThrow(
            () -> TableEntity.validateEntity(GoodEntity.class),
            "Expected minimal valid entity to pass validation"
        );
    }

    /**
     * Scenario V2: valid entity with method-level @PrimaryKey should pass validation.
     */
    @TableName("method_pk_entity")
    static class MethodPkEntity implements TableEntity {
        @TableColumn private final UUID id;
        @TableColumn private final String name;
        @TableConstructor
        public MethodPkEntity(UUID id, String name) { this.id = id; this.name = name; }
        @PrimaryKey
        public UUID getId() { return this.id; }
    }
    @Test
    void validMethodPkEntityShouldPass() {
        Assertions.assertDoesNotThrow(
            () -> TableEntity.validateEntity(MethodPkEntity.class),
            "Expected method-level PK entity to pass validation"
        );
    }

    /**
     * Scenario V3: valid entity with a non-column (unannotated) field should pass validation.
     */
    @TableName("ignore_field_entity")
    static class IgnoreFieldEntity implements TableEntity {
        @PrimaryKey @TableColumn private final UUID id;
        @TableConstructor
        public IgnoreFieldEntity(UUID id) { this.id = id; }
        private String tempData;
    }
    @Test
    void validEntityWithUnannotatedFieldsShouldPass() {
        Assertions.assertDoesNotThrow(
            () -> TableEntity.validateEntity(IgnoreFieldEntity.class),
            "Expected entity with unannotated field to pass validation"
        );
    }
    // 8) @ForeignKey on non-TableEntity type should fail
    @TableName("bad_fk_entity")
    static class BadForeignKeyEntity implements TableEntity {
        @PrimaryKey @TableColumn private final UUID id;
        @ForeignKey @TableColumn private final String notEntity;  // wrong type
        @TableConstructor
        public BadForeignKeyEntity(UUID id, String notEntity) {
            this.id = id;
            this.notEntity = notEntity;
        }
    }
    /**
     * Scenario 8: @ForeignKey on non-TableEntity type should throw IllegalStateException.
     */
    @Test
    void foreignKeyOnNonEntityShouldThrow() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> TableEntity.validateEntity(BadForeignKeyEntity.class),
            "Expected @ForeignKey on non-TableEntity type to fail"
        );
    }

    // 9) @ForeignKey on a TableEntity type should pass
    @TableName("good_fk_entity")
    static class GoodForeignKeyEntity implements TableEntity {
        @PrimaryKey @TableColumn private final UUID id;
        @ForeignKey @TableColumn private final GoodEntity ref;  // GoodEntity implements TableEntity
        @TableConstructor
        public GoodForeignKeyEntity(UUID id, GoodEntity ref) {
            this.id = id;
            this.ref = ref;
        }
    }
    /**
     * Scenario 9: @ForeignKey on TableEntity type should pass validation.
     */
    @Test
    void foreignKeyOnEntityShouldPass() {
        Assertions.assertDoesNotThrow(
            () -> TableEntity.validateEntity(GoodForeignKeyEntity.class),
            "Expected @ForeignKey on TableEntity type to pass"
        );
    }

    // 10) @TableColumn on primitive type should fail
    @TableName("primitive_column_entity")
    static class PrimitiveColumnEntity implements TableEntity {
        @PrimaryKey @TableColumn private final UUID id;
        @TableColumn private final int count;  // illegal: primitive type
        @TableConstructor
        public PrimitiveColumnEntity(UUID id, int count) {
            this.id = id;
            this.count = count;
        }
    }
    /**
     * Scenario 10: @TableColumn on a primitive type should throw IllegalStateException.
     */
    @Test
    void tableColumnOnPrimitiveShouldThrow() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> TableEntity.validateEntity(PrimitiveColumnEntity.class),
            "Expected @TableColumn on primitive type to fail"
        );
    }

    // 11) @TableColumn on a wrapper type should pass
    @TableName("wrapper_column_entity")
    static class WrapperColumnEntity implements TableEntity {
        @PrimaryKey @TableColumn private final UUID id;
        @TableColumn private final Integer count;  // ok: wrapper type
        @TableConstructor
        public WrapperColumnEntity(UUID id, Integer count) {
            this.id = id;
            this.count = count;
        }
    }
    /**
     * Scenario 11: @TableColumn on a wrapper (non-primitive) type should pass validation.
     */
    @Test
    void tableColumnOnWrapperShouldPass() {
        Assertions.assertDoesNotThrow(
            () -> TableEntity.validateEntity(WrapperColumnEntity.class),
            "Expected @TableColumn on wrapper type to pass"
        );
    }
}