package com.amazon.ionschema.model

/**
 * Represents a top-level, named type definition.
 */
@ExperimentalIonSchemaModel
data class NamedTypeDefinition(val typeName: String, val typeDefinition: TypeDefinition)
