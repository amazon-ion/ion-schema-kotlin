package com.amazon.ionschema.cli.util

import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.IonElement
import com.amazon.ionelement.api.StructElement
import com.amazon.ionelement.api.SymbolElement
import com.amazon.ionelement.api.ionString
import com.amazon.ionelement.api.ionStructOf
import com.amazon.ionelement.api.ionSymbol
import com.amazon.ionelement.api.toIonElement
import com.amazon.ionschema.Type

/**
 * Checks if a type name is one of the Ion Schema built in types.
 */
fun isBuiltInTypeName(name: String) = name in setOf(
    "any", "\$any",
    "blob", "\$blob",
    "bool", "\$bool",
    "clob", "\$clob",
    "decimal", "\$decimal",
    "document",
    "float", "\$float",
    "int", "\$int",
    "list", "\$list",
    "lob", "\$lob",
    "nothing",
    "\$null",
    "number", "\$number",
    "sexp", "\$sexp",
    "string", "\$string",
    "struct", "\$struct",
    "symbol", "\$symbol",
    "text", "\$text",
    "timestamp", "\$timestamp",
)

/**
 * Recursively finds all type references in an Ion Schema type definition.
 */
fun findTypeReferences(ionElement: IonElement): List<AnyElement> {
    ionElement as AnyElement
    return when {
        ionElement.isInlineImport() -> listOf(ionElement)
        ionElement is SymbolElement -> listOf(ionElement)
        ionElement is StructElement -> {
            ionElement.fields.flatMap {
                when (it.name) {
                    "type",
                    "not",
                    "element" -> findTypeReferences(it.value)
                    "one_of",
                    "any_of",
                    "all_of",
                    "ordered_elements",
                    "fields" -> it.value.containerValues.flatMap { findTypeReferences(it) }
                    else -> emptyList()
                }
            }
        }
        else -> emptyList()
    }
}

/**
 * Determines whether one header import subsumes another header import.
 */
infix fun StructElement.includesTypeImport(other: StructElement): Boolean {
    return this["id"].textValue == other["id"].textValue &&
        (this.getOptional("type") ?: other["type"]).textValue == other["type"].textValue &&
        this.getOptional("as")?.textValue == other.getOptional("as")?.textValue
}

/**
 * Checks whether an [IonElement] is an Ion Schema inline import
 */
fun IonElement.isInlineImport(): Boolean =
    this is StructElement && this.fields.map { it.name }
        .let { fieldNames -> "id" in fieldNames && "type" in fieldNames }

/**
 * Constructs a [StructElement] that is a valid Ion Schema import for the given [Type].
 */
fun createImportForType(type: Type): StructElement {
    val alias = type.name

    val typeIsl = type.isl.toIonElement()
    val name = when (typeIsl) {
        is StructElement -> typeIsl["name"].textValue
        is SymbolElement -> typeIsl.textValue
        else -> TODO("Unreachable!")
    }
    val importedFromSchemaId = type.schemaId!!
    return if (alias != name) {
        ionStructOf(
            "id" to ionString(importedFromSchemaId),
            "type" to ionSymbol(name),
            "as" to ionSymbol(alias),
        )
    } else {
        ionStructOf(
            "id" to ionString(importedFromSchemaId),
            "type" to ionSymbol(name),
        )
    }
}
