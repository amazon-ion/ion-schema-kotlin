package com.amazon.ionschema.model

import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion

/**
 * Represents an Ion Schema document.
 */
@ExperimentalIonSchemaModel
data class SchemaDocument(
    val id: String?,
    val items: List<Item>
) {
    val ionSchemaVersion: IonSchemaVersion = items.firstOrNull { it !is Item.OpenContent }
        ?.let { it as? Item.VersionMarker }
        ?.value
        ?: IonSchemaVersion.v1_0
    val header: Item.Header? = items.filterIsInstance<Item.Header>().singleOrNull()
    val footer: Item.Footer? = items.filterIsInstance<Item.Footer>().singleOrNull()
    val declaredTypes: List<NamedTypeDefinition> = items.filterIsInstance<Item.Type>().map { it.value }

    /**
     * Represents a top-level item in a schema document.
     */
    sealed class Item {
        data class VersionMarker(val value: IonSchemaVersion) : Item()

        data class Type(val value: NamedTypeDefinition) : Item()

        data class Header(
            val imports: List<HeaderImport> = emptyList(),
            val userReservedFields: UserReservedFields = UserReservedFields(),
            val openContent: OpenContentFields = emptyList()
        ) : Item()

        data class Footer(val openContent: OpenContentFields = emptyList()) : Item()

        data class OpenContent(val value: IonValue) : Item()
    }
}
