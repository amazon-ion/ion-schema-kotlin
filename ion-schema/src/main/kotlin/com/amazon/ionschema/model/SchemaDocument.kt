package com.amazon.ionschema.model

import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.islRequire

/**
 * Represents an Ion Schema document.
 */
@ExperimentalIonSchemaModel
data class SchemaDocument(
    val id: String?,
    val ionSchemaVersion: IonSchemaVersion,
    val items: List<Content>
) {
    val header: SchemaHeader? = items.filterIsInstance<SchemaHeader>().singleOrNull()
    val footer: SchemaFooter? = items.filterIsInstance<SchemaFooter>().singleOrNull()
    val declaredTypes: Map<String, NamedTypeDefinition> = let {
        val typeList = items.filterIsInstance<NamedTypeDefinition>()
        val typeMap = typeList.associateBy { it.typeName }
        islRequire(typeMap.size == typeList.size) {
            "Conflicting type names in schema"
        }
        typeMap
    }

    /**
     * Represents a top-level value in a schema document.
     * Implemented by [NamedTypeDefinition], [SchemaHeader], [SchemaFooter], and [OpenContent].
     * This interface is not intended to be implemented by users of the library.
     */
    interface Content

    /**
     * Represents top-level open content in a SchemaDocument.
     */
    data class OpenContent(val value: IonValue) : Content
}
