// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer

import com.amazon.ion.IonWriter
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.writer.internal.IonSchemaWriterV2_0
import com.amazon.ionschema.writer.internal.VersionedIonSchemaWriter

/**
 * Writes Ion Schema model to an IonWriter.
 */
@ExperimentalIonSchemaModel
object IonSchemaWriter {
    /**
     * Writes a [SchemaDocument].
     */
    @JvmStatic
    fun writeSchema(ionWriter: IonWriter, schemaDocument: SchemaDocument) {
        val delegate: VersionedIonSchemaWriter = when (schemaDocument.ionSchemaVersion) {
            IonSchemaVersion.v1_0 -> TODO("IonSchemaWriter does not support ISL 1.0")
            IonSchemaVersion.v2_0 -> IonSchemaWriterV2_0
        }
        delegate.writeSchema(ionWriter, schemaDocument)
    }
}
