package com.amazon.ionschema.internal

import com.amazon.ionschema.IonSchemaSystem
import com.amazon.ionschema.IonSchemaSystemBuilder

/**
 * Specific strings that can appear as prefixes to warning messages that are
 * emitted by an IonSchemaSystem. They are listed here so that documentation
 * about specific warnings is easier to find.
 *
 * Warning messages are not guaranteed to be prefixed with one of these strings.
 */
internal enum class WarningType {
    /**
     * Indicates that a type was imported transitively. For example, given
     * schemas A, B, and C, and given that A imports B and B imports C, the
     * IonSchemaSystem would issue this warning if it detects that schema A
     * references a type that was declared in schema C.
     *
     * The import resolution behavior in `ion-schema-kotlin` that allowed this
     * to happen is incorrect and will be deprecated in future versions
     * of `ion-schema-kotlin`.
     *
     * You can disable this incorrect import resolution behavior now by
     * setting [IonSchemaSystemBuilder.allowTransitiveImports] to `false`,
     * but be aware that it is a potentially breaking change, and you may
     * need to update some of your schemas to work with the fixed behavior.
     */
    INVALID_TRANSITIVE_IMPORT,

    /**
     * Indicates that a schema has no declared types. Such a schema is usually
     * useless because there are no types that can be used to validate anything.
     *
     * Alternately, the schema might have no declared types because it relies
     * on transitive imports to aggregate types from other schemas into one
     * schema. This particular use case will be broken in a future release when
     * the incorrect import resolution behavior is deprecated. (For more details,
     * refer to the documentation of [INVALID_TRANSITIVE_IMPORT].) To aggregate
     * types from multiple schemas into a single schema without using transitive
     * imports, you can explicitly redeclare each type you wish to aggregate
     * using an inline import. For example:
     * ```
     * type::{
     *   name: my_type_name,
     *   type: { id: my_other_schema, type: my_type_name }
     * }
     * ```
     *
     * Finally, a schema with no types can still contain user content, so it may
     * still be useful in some other contexts outside of [IonSchemaSystem].
     */
    SCHEMA_HAS_NO_TYPES
}

/**
 * Builds a message that indicates that an imported type was imported transitively.
 */
internal fun warnInvalidTransitiveImport(importedType: ImportedType, importedToSchemaId: String?) =
    "${WarningType.INVALID_TRANSITIVE_IMPORT} in '${importedToSchemaId ?: "<unnamed schema>"}'. " +
        "Type '${importedType.name}' was imported from '${importedType.importedFromSchemaId}', " +
        "but is actually from '${importedType.schemaId}'."
