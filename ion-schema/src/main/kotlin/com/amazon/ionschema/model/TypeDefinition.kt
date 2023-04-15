package com.amazon.ionschema.model

/**
 * Represents the common fields of all type definitions; used to compose [NamedTypeDefinition] and [TypeArgument.InlineType].
 */
@ExperimentalIonSchemaModel
class TypeDefinition(val constraints: List<Constraint>, val openContent: OpenContentFields = emptyList()) {
    override fun equals(other: Any?): Boolean {
        return other is TypeDefinition &&
            this.constraints == other.constraints &&
            this.openContent == other.openContent
    }

    override fun hashCode(): Int {
        return constraints.hashCode() * 31 + openContent.hashCode()
    }
}
