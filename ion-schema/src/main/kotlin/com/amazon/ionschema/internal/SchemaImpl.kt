package com.amazon.ionschema.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Schema

internal interface SchemaImpl : Schema {
    val schemaId: String?

    fun addDeferredType(typeRef: TypeReferenceDeferred)

    companion object {
        operator fun invoke(
            schemaSystem: IonSchemaSystemImpl,
            schemaCores: Map<IonSchemaVersion, SchemaCore>,
            schemaContent: Iterator<IonValue>,
            schemaId: String?
        ): Schema {
            val content = schemaContent.asSequence().toList()
            return when (val version = findIslVersion(content)) {
                IonSchemaVersion.v1_0 -> SchemaImpl_1_0(schemaSystem, schemaCores[version]!!, content.iterator(), schemaId)
                IonSchemaVersion.v2_0 -> SchemaImpl_2_0(schemaSystem, schemaCores[version]!!, content.iterator(), schemaId)
            }
        }
    }
}

private val ISL_VERSION_MARKER = Regex("^\\\$ion_schema_\\d.*")

private fun findIslVersion(schemaContent: Iterable<IonValue>): IonSchemaVersion {
    for (value in schemaContent) {
        // could be a version, header, type, or open content
        when (value) {
            is IonSymbol -> if (ISL_VERSION_MARKER.matches(value.stringValue())) {
                return when (value.stringValue()) {
                    "\$ion_schema_1_0" -> IonSchemaVersion.v1_0
                    "\$ion_schema_2_0" -> IonSchemaVersion.v2_0
                    else -> throw InvalidSchemaException("Not a supported ISL Version: ${value.stringValue()}")
                }
            } else {
                // `value` is open content
            }
            is IonStruct -> if (value.hasTypeAnnotation("type") || value.hasTypeAnnotation("schema_header")) {
                return IonSchemaVersion.v1_0
            }
            else -> {} // `value` is open content
        }
    }
    return IonSchemaVersion.v1_0
}
