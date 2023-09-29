// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument.Reference
import com.amazon.ionschema.model.required

@OptIn(ExperimentalIonSchemaModel::class)
class OrderedElementsWriterTest : ConstraintTestBase(
    writer = OrderedElementsWriter(stubTypeWriterWithRefs("foo", "bar")),
    expectedConstraints = setOf(Constraint.OrderedElements::class),
    writeTestCases = listOf(
        Constraint.OrderedElements(emptyList()) to "ordered_elements: []",
        Constraint.OrderedElements(Reference("foo").required()) to "ordered_elements: [foo]",
        Constraint.OrderedElements(
            Reference("foo").required(),
            Reference("bar").required(),
            Reference("foo").required(),
        ) to "ordered_elements: [foo, bar, foo]",
    )
)
