package com.amazon.ionschema.model

import com.amazon.ionschema.util.emptyBag

/**
 * Represents the schema footer for all versions of Ion Schema.
 */
@ExperimentalIonSchemaModel
data class SchemaFooter(val openContent: OpenContentFields = emptyBag()) : SchemaDocument.Content
