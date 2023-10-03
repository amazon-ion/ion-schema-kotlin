// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal

import com.amazon.ion.IonWriter
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.SchemaFooter

@ExperimentalIonSchemaModel
object FooterWriter {
    fun writeFooter(ionWriter: IonWriter, schemaFooter: SchemaFooter) {
        ionWriter.setTypeAnnotations("schema_footer")
        ionWriter.writeStruct {
            for ((fieldName, fieldValue) in schemaFooter.openContent) {
                ionWriter.setFieldName(fieldName)
                fieldValue.writeTo(ionWriter)
            }
        }
    }
}
