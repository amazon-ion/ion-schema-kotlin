// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.IonSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalIonSchemaModel::class)
class ElementWriterTest : ConstraintTestBase(
    writer = ElementWriter(stubTypeWriterWithRefs("foo_type"), IonSchemaVersion.v2_0),
    expectedConstraints = setOf(Constraint.Element::class),
    writeTestCases = listOf(
        Constraint.Element(TypeArgument.Reference("foo_type")) to "element: foo_type",
        Constraint.Element(TypeArgument.Reference("foo_type"), distinct = true) to "element: distinct::foo_type",
    )
) {
    @Test
    fun `writer should throw exception when distinct = true and version = v1_0`() {
        val writer = ElementWriter(stubTypeWriterWithRefs("foo_type"), IonSchemaVersion.v1_0)
        val constraint = Constraint.Element(TypeArgument.Reference("foo_type"), distinct = true)
        assertThrows<IonSchemaException> {
            writer.writeTo(mockk(relaxed = true), constraint)
        }
    }
}
