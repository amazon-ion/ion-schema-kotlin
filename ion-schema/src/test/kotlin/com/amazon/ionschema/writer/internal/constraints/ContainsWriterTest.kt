// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint.Contains
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

@OptIn(ExperimentalIonSchemaModel::class)
class ContainsWriterTest : ConstraintTestBase(
    writer = ContainsWriter,
    expectedConstraints = setOf(Contains::class),
    writeTestCases = listOf(
        Contains(emptySet()) to "contains: []",
        Contains(setOf(ion("[foo, true]"), ion("bar"))) to "contains: [[foo, true], bar]",
        Contains(setOf(ion("me::2"), ion("null.timestamp"), ion("{a:b}"))) to "contains: [me::2, null.timestamp, {a:b}]",
    )
)
