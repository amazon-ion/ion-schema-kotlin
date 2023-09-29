// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

@OptIn(ExperimentalIonSchemaModel::class)
class PrecisionWriterTest : ConstraintTestBase(
    writer = PrecisionWriter,
    expectedConstraints = setOf(Constraint.Precision::class),
    writeTestCases = listOf(
        Constraint.Precision(DiscreteIntRange(2, 5)) to "precision: range::[2, 5]",
        Constraint.Precision(DiscreteIntRange(null, 23)) to "precision: range::[min, 23]",
        Constraint.Precision(DiscreteIntRange(7, null)) to "precision: range::[7, max]",
        Constraint.Precision(DiscreteIntRange(3, 3)) to "precision: 3",
    )
)
