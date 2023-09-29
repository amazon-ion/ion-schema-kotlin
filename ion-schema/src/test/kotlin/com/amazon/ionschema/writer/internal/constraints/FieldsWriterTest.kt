// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.occurs
import com.amazon.ionschema.model.optional
import org.junit.jupiter.api.Test

@OptIn(ExperimentalIonSchemaModel::class)
class FieldsWriterTest : ConstraintTestBase(
    writer = FieldsWriter(stubTypeWriterWithRefs("foo_type", "bar_type"), IonSchemaVersion.v2_0),
    expectedConstraints = setOf(Constraint.Fields::class),
    writeTestCases = listOf(
        Constraint.Fields(fieldsMap, closed = true) to "fields: closed::{ a: foo_type, b: bar_type }",
        Constraint.Fields(fieldsMap, closed = false) to "fields: { a: foo_type, b: bar_type }",
    )
) {
    companion object {
        private val fieldsMap = mapOf(
            "a" to TypeArgument.Reference("foo_type").optional(),
            "b" to TypeArgument.Reference("bar_type").occurs(0, 1),
        )
    }

    @Test
    fun `writer should write content closed for v1_0`() {
        val writer = FieldsWriter(stubTypeWriterWithRefs("foo_type", "bar_type"), IonSchemaVersion.v1_0)
        runWriteCase(
            writer,
            Constraint.Fields(fieldsMap, closed = true) to "content: closed, fields: { a: foo_type, b: bar_type }"
        )
    }
}
