// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

@OptIn(ExperimentalIonSchemaModel::class)
class ExponentWriterTest : ConstraintTestBase(
    writer = ExponentWriter,
    expectedConstraints = setOf(Constraint.Exponent::class),
    writeTestCases = listOf(
        Constraint.Exponent(DiscreteIntRange(2, 5)) to "exponent: range::[2, 5]",
        Constraint.Exponent(DiscreteIntRange(null, 23)) to "exponent: range::[min, 23]",
        Constraint.Exponent(DiscreteIntRange(7, null)) to "exponent: range::[7, max]",
        Constraint.Exponent(DiscreteIntRange(3, 3)) to "exponent: 3",
    ),
)
