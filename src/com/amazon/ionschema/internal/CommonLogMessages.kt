package com.amazon.ionschema.internal

internal object CommonLogMessages {
    /**
     * Builds a message that indicates that an imported type was imported transitively.
     */
    fun invalidTransitiveImport(importedType: ImportedType, importedToSchemaId: String?) =
        "INVALID_TRANSITIVE_IMPORT in '${importedToSchemaId ?: "<unnamed schema>"}'. " +
            "Type '${importedType.name}' was imported from '${importedType.importedFromSchemaId}', " +
            "but is actually from '${importedType.schemaId}'."
}
