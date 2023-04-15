package com.amazon.ionschema.model

/**
 * A TypeArgument represents (defines or references) a Type, allowing the Type to be used as an argument for a constraint.
 *
 * **_Do not implement this interface_**—It will become a sealed interface once `ion-schema-kotlin` is updated to use
 * language version 1.5.
 */
@ExperimentalIonSchemaModel
sealed class TypeArgument {
    abstract val nullability: Nullability

    /**
     * Nullability modifiers for [TypeArgument].
     */
    enum class Nullability {
        /**
         * No special treatment of null values.
         */
        None,
        /**
         * Ion Schema 1.0 nullability. See https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#core-types
         */
        Nullable,
        /**
         * Ion Schema 2.0+ `$null_or` annotation. See https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#nullable-type-arguments
         */
        OrNull,
    }

    /**
     * [TypeArgument] that is an anonymous type, defined inline.
     */
    class InlineType(val typeDefinition: TypeDefinition, override val nullability: Nullability = Nullability.None) : TypeArgument()

    /**
     * A [TypeArgument] that references another type by [typeName] only.
     * This can refer to any types that are defined in the same schema, imported via the schema header, or any built-in types.
     */
    class Reference(val typeName: String, override val nullability: Nullability = Nullability.None) : TypeArgument()

    /**
     * A [TypeArgument] that references a type from a different schema by [schemaId] and [typeName].
     */
    class Import(val schemaId: String, val typeName: String, override val nullability: Nullability = Nullability.None) : TypeArgument()
}
