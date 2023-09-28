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
                writeToList(schemaHeader.imports) { writeImport(it) }
            }
            if (schemaHeader.userReservedFields != UserReservedFields.EMPTY) {
                ionWriter.writeUserReservedFields(schemaHeader.userReservedFields)
            }

            for ((fieldName, fieldValue) in schemaHeader.openContent) {
                ionWriter.setFieldName(fieldName)
                fieldValue.writeTo(ionWriter)
            }
        }
    }

    private fun IonWriter.writeUserReservedFields(userReservedFields: UserReservedFields) {
        setFieldName("user_reserved_fields")
        writeStruct {
            if (userReservedFields.header.isNotEmpty()) {
                setFieldName("schema_header")
                writeToList(userReservedFields.header) { writeSymbol(it) }
            }
            if (userReservedFields.type.isNotEmpty()) {
                setFieldName("type")
                writeToList(userReservedFields.type) { writeSymbol(it) }
            }
            if (userReservedFields.footer.isNotEmpty()) {
                setFieldName("schema_footer")
                writeToList(userReservedFields.footer) { writeSymbol(it) }
            }
        }
    }

    private fun IonWriter.writeImport(import: HeaderImport) {
        writeStruct {
            when (import) {
                is HeaderImport.Type -> {
                    setFieldName("id")
                    writeString(import.id)
                    setFieldName("type")
                    writeSymbol(import.targetType)
                    import.asType?.let {
                        setFieldName("as")
                        writeSymbol(it)
                    }
                }
                is HeaderImport.Wildcard -> {
                    setFieldName("id")
                    writeString(import.id)
                }
            }
        }
    }
}
