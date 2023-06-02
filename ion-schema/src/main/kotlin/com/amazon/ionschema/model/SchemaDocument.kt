package com.amazon.ionschema.model

import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.util.emptyBag

/**
 * Represents an Ion Schema document.
 */
@ExperimentalIonSchemaModel
data class SchemaDocument(
    val id: String?,
    val ionSchemaVersion: IonSchemaVersion,
    val items: List<Item>
) {
    val header: Item.Header? = items.filterIsInstance<Item.Header>().singleOrNull()
    val footer: Item.Footer? = items.filterIsInstance<Item.Footer>().singleOrNull()
    val declaredTypes: Map<String, NamedTypeDefinition> = let {
        val typeList = items.filterIsInstance<Item.Type>().map { it.value }
        val typeMap = typeList.associateBy { it.typeName }
        islRequire(typeMap.size == typeList.size) {
            "Conflicting type names in schema"
        }
        typeMap
    }

    /**
     * Represents a top-level item in a schema document.
     */
    sealed class Item {
        data class Type(val value: NamedTypeDefinition) : Item()

        data class Header(
            val imports: Set<HeaderImport> = emptySet(),
            val userReservedFields: UserReservedFields = UserReservedFields(),
            val openContent: OpenContentFields = emptyBag()
        ) : Item()

        data class Footer(val openContent: OpenContentFields = emptyBag()) : Item()

        data class OpenContent(val value: IonValue) : Item()
    }
}
