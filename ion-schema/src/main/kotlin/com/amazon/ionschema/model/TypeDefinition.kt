package com.amazon.ionschema.model

import com.amazon.ionschema.util.emptyBag

/**
 * Represents the common fields of all type definitions; used to compose [NamedTypeDefinition] and [TypeArgument.InlineType].
 *
 * Constraints are modeled as a [Set] because if there are two identical constraints, they are redundant.
 */
@ExperimentalIonSchemaModel
data class TypeDefinition(val constraints: Set<Constraint>, val openContent: OpenContentFields = emptyBag()) {
    override fun equals(other: Any?): Boolean {
        return other is TypeDefinition &&
            this.constraints == other.constraints &&
            this.openContent == other.openContent
    }

    override fun hashCode(): Int {
        return constraints.hashCode() * 31 + openContent.hashCode()
    }
}
