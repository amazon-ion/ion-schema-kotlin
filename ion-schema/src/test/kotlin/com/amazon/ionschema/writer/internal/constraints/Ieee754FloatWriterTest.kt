// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.Ieee754InterchangeFormat

@OptIn(ExperimentalIonSchemaModel::class)
class Ieee754FloatWriterTest : ConstraintTestBase(
    writer = Ieee754FloatWriter,
    expectedConstraints = setOf(Constraint.Ieee754Float::class),
    writeTestCases = listOf(
        Constraint.Ieee754Float(Ieee754InterchangeFormat.Binary16) to "ieee754_float: binary16",
        Constraint.Ieee754Float(Ieee754InterchangeFormat.Binary32) to "ieee754_float: binary32",
        Constraint.Ieee754Float(Ieee754InterchangeFormat.Binary64) to "ieee754_float: binary64",
    )
)
