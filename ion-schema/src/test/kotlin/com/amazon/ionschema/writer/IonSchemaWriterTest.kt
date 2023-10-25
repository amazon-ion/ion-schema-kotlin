// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer

import com.amazon.ionschema.ION
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.assertEqualIon
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.SchemaDocument
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalIonSchemaModel::class)
class IonSchemaWriterTest {
    // The purpose of these tests is just to check that it delegates to the correct writer implementation.
    // Testing the actual serialization is done in WriterTests.kt

    @Test
    fun `IonSchemaWriter throw UnsupportedOperationException for ISL 1 0`() {
        val writer = ION.newTextWriter(StringBuilder())
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v1_0, emptyList())
        assertThrows<NotImplementedError> {
            IonSchemaWriter.writeSchema(writer, schema)
        }
    }

    @Test
    fun `IonSchemaWriter writes a schema document for ISL 2 0`() {
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, emptyList())
        // Since there's no content added to the schema, we expect just a version marker
        assertEqualIon("\$ion_schema_2_0 ") {
            IonSchemaWriter.writeSchema(it, schema)
        }
    }
}
