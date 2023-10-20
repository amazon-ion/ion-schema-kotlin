// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint.AnnotationsV2
import com.amazon.ionschema.model.Constraint.AnnotationsV2.Modifier
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument

@OptIn(ExperimentalIonSchemaModel::class)
class AnnotationsV2WriterTest : ConstraintTestBase(
    writer = AnnotationsV2Writer(stubTypeWriterWithRefs("foo_type")),
    expectedConstraints = setOf(
        AnnotationsV2.Simplified::class,
        AnnotationsV2.Standard::class,
    ),
    writeTestCases = listOf(
        AnnotationsV2.Standard(TypeArgument.Reference("foo_type")) to "annotations: foo_type",
        AnnotationsV2.Simplified(Modifier.Closed, setOf("a")) to "annotations: closed::[a]",
        AnnotationsV2.Simplified(Modifier.Required, setOf("b")) to "annotations: required::[b]",
        AnnotationsV2.Simplified(Modifier.Exact, setOf("c")) to "annotations: closed::required::[c]",
    )
)
