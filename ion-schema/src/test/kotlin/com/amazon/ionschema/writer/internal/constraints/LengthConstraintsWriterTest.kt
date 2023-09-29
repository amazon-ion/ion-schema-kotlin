// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

@OptIn(ExperimentalIonSchemaModel::class)
class LengthConstraintsWriterTest : ConstraintTestBase(
    writer = LengthConstraintsWriter,
    expectedConstraints = setOf(
        Constraint.ByteLength::class,
        Constraint.CodepointLength::class,
        Constraint.ContainerLength::class,
        Constraint.Utf8ByteLength::class,
    ),
    writeTestCases = listOf(
        Constraint.ByteLength(DiscreteIntRange(2, 5)) to "byte_length: range::[2, 5]",
        Constraint.ByteLength(DiscreteIntRange(null, 23)) to "byte_length: range::[min, 23]",
        Constraint.ByteLength(DiscreteIntRange(7, null)) to "byte_length: range::[7, max]",
        Constraint.ByteLength(DiscreteIntRange(3, 3)) to "byte_length: 3",
        Constraint.CodepointLength(DiscreteIntRange(2, 5)) to "codepoint_length: range::[2, 5]",
        Constraint.CodepointLength(DiscreteIntRange(null, 23)) to "codepoint_length: range::[min, 23]",
        Constraint.CodepointLength(DiscreteIntRange(7, null)) to "codepoint_length: range::[7, max]",
        Constraint.CodepointLength(DiscreteIntRange(3, 3)) to "codepoint_length: 3",
        Constraint.CodepointLength(DiscreteIntRange(2, 5)) to "codepoint_length: range::[2, 5]",
        Constraint.ContainerLength(DiscreteIntRange(null, 23)) to "container_length: range::[min, 23]",
        Constraint.ContainerLength(DiscreteIntRange(7, null)) to "container_length: range::[7, max]",
        Constraint.ContainerLength(DiscreteIntRange(3, 3)) to "container_length: 3",
        Constraint.Utf8ByteLength(DiscreteIntRange(2, 5)) to "utf8_byte_length: range::[2, 5]",
        Constraint.Utf8ByteLength(DiscreteIntRange(null, 23)) to "utf8_byte_length: range::[min, 23]",
        Constraint.Utf8ByteLength(DiscreteIntRange(7, null)) to "utf8_byte_length: range::[7, max]",
        Constraint.Utf8ByteLength(DiscreteIntRange(3, 3)) to "utf8_byte_length: 3",
    )
)
