// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument

@OptIn(ExperimentalIonSchemaModel::class)
class FieldNamesWriterTest : ConstraintTestBase(
    writer = FieldNamesWriter(stubTypeWriterWithRefs("foo_type")),
    expectedConstraints = setOf(Constraint.FieldNames::class),
    writeTestCases = listOf(
        Constraint.FieldNames(TypeArgument.Reference("foo_type")) to "field_names: foo_type",
        Constraint.FieldNames(TypeArgument.Reference("foo_type"), distinct = true) to "field_names: distinct::foo_type",
    )
)
