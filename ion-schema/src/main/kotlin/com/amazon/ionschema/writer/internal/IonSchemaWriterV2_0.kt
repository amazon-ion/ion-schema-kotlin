// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonWriter
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.model.SchemaFooter
import com.amazon.ionschema.model.SchemaHeader
import com.amazon.ionschema.model.TypeDefinition

@ExperimentalIonSchemaModel
internal object IonSchemaWriterV2_0 : VersionedIonSchemaWriter {

    private val headerWriter = HeaderWriter
    private val typeWriter = TypeWriterV2_0
    private val footerWriter = FooterWriter

    override fun writeSchema(ionWriter: IonWriter, schemaDocument: SchemaDocument) {
        islRequire(schemaDocument.ionSchemaVersion == IonSchemaVersion.v2_0) { "IonSchemaWriterV2_0 only supports ISL 2.0" }
        ionWriter.writeSymbol("\$ion_schema_2_0")
        for (item in schemaDocument.items) {
            when (item) {
                is SchemaHeader -> headerWriter.writeHeader(ionWriter, item)
                is NamedTypeDefinition -> writeNamedType(ionWriter, item)
                is SchemaFooter -> footerWriter.writeFooter(ionWriter, item)
                is SchemaDocument.OpenContent -> item.value.writeTo(ionWriter)
            }
        }
    }

    override fun writeType(ionWriter: IonWriter, typeDefinition: TypeDefinition) {
        typeWriter.writeOrphanedTypeDefinition(ionWriter, typeDefinition)
    }

    override fun writeNamedType(ionWriter: IonWriter, namedTypeDefinition: NamedTypeDefinition) {
        typeWriter.writeNamedTypeDefinition(ionWriter, namedTypeDefinition)
    }
}
