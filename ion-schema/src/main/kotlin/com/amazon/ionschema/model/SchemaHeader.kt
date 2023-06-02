package com.amazon.ionschema.model

import com.amazon.ionschema.util.emptyBag

/**
 * Represents the schema header for all versions of Ion Schema.
 */
@ExperimentalIonSchemaModel
data class SchemaHeader(
    val imports: Set<HeaderImport> = emptySet(),
    val userReservedFields: UserReservedFields = UserReservedFields(),
    val openContent: OpenContentFields = emptyBag()
) : SchemaDocument.Content
