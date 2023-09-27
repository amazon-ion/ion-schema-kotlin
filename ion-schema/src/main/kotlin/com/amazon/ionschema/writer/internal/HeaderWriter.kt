// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.HeaderImport
import com.amazon.ionschema.model.SchemaHeader
import com.amazon.ionschema.model.UserReservedFields

@ExperimentalIonSchemaModel
object HeaderWriter {
    fun writeHeader(ionWriter: IonWriter, schemaHeader: SchemaHeader) {
        ionWriter.setTypeAnnotations("schema_header")
        ionWriter.writeStruct {
            if (schemaHeader.imports.isNotEmpty()) {
                setFieldName("imports")
                writeList {
                    schemaHeader.imports.forEach { it.writeTo(ionWriter) }
                }
            }
            if (schemaHeader.userReservedFields != UserReservedFields()) {
                setFieldName("user_reserved_fields")
                writeStruct {
                    if (schemaHeader.userReservedFields.header.isNotEmpty()) {
                        setFieldName("schema_header")
                        writeList { schemaHeader.userReservedFields.header.forEach { writeSymbol(it) } }
                    }
                    if (schemaHeader.userReservedFields.type.isNotEmpty()) {
                        setFieldName("type")
                        writeList { schemaHeader.userReservedFields.type.forEach { writeSymbol(it) } }
                    }
                    if (schemaHeader.userReservedFields.footer.isNotEmpty()) {
                        setFieldName("schema_footer")
                        writeList { schemaHeader.userReservedFields.footer.forEach { writeSymbol(it) } }
                    }
                }
            }

            for ((fieldName, fieldValue) in schemaHeader.openContent) {
                ionWriter.setFieldName(fieldName)
                fieldValue.writeTo(ionWriter)
            }
        }
    }

    private fun HeaderImport.writeTo(ionWriter: IonWriter) {
        ionWriter.writeStruct {
            when (this@writeTo) {
                is HeaderImport.Type -> {
                    setFieldName("id")
                    writeString(id)
                    setFieldName("type")
                    writeSymbol(targetType)
                    if (asType != null) {
                        setFieldName("as")
                        writeSymbol(asType)
                    }
                }
                is HeaderImport.Wildcard -> {
                    setFieldName("id")
                    writeString(id)
                }
            }
        }
    }
}
