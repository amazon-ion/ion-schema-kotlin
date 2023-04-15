package com.amazon.ionschema.model

/**
 * Represents an import in the schema header.
 */
@ExperimentalIonSchemaModel
sealed class HeaderImport {
    /**
     * An import that imports all types from a schema
     */
    data class Wildcard(val id: String) : HeaderImport()
    /**
     * An import of a specific type from a schema, with an optional alias.
     */
    data class Type(val id: String, val targetType: String, val asType: String? = null) : HeaderImport()
}
