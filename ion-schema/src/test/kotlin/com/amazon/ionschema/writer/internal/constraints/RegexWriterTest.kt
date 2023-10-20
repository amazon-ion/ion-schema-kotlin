// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel

@OptIn(ExperimentalIonSchemaModel::class)
class RegexWriterTest : ConstraintTestBase(
    writer = RegexWriter,
    expectedConstraints = setOf(Constraint.Regex::class),
    writeTestCases = listOf(
        Constraint.Regex("abc") to """ regex: "abc" """,
        Constraint.Regex("abc", multiline = true) to """ regex: m::"abc" """,
        Constraint.Regex("abc", caseInsensitive = true) to """ regex: i::"abc" """,
        Constraint.Regex("abc", true, true) to """ regex: i::m::"abc" """,
    )
)
