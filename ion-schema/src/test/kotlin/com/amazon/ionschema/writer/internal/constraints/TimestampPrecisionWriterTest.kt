// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ContinuousRange.Limit.Closed
import com.amazon.ionschema.model.ContinuousRange.Limit.Open
import com.amazon.ionschema.model.ContinuousRange.Limit.Unbounded
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TimestampPrecisionRange
import com.amazon.ionschema.model.TimestampPrecisionValue

@OptIn(ExperimentalIonSchemaModel::class)
class TimestampPrecisionWriterTest : ConstraintTestBase(
    writer = TimestampPrecisionWriter,
    expectedConstraints = setOf(Constraint.TimestampPrecision::class),
    writeTestCases = listOf(
        Constraint.TimestampPrecision(TimestampPrecisionRange(TimestampPrecisionValue.Second)) to "timestamp_precision: second",
        Constraint.TimestampPrecision(TimestampPrecisionRange(Unbounded, Closed(TimestampPrecisionValue.Day))) to "timestamp_precision: range::[min, day]",
        Constraint.TimestampPrecision(TimestampPrecisionRange(Open(TimestampPrecisionValue.Year), Closed(TimestampPrecisionValue.Day))) to "timestamp_precision: range::[exclusive::year, day]",
        Constraint.TimestampPrecision(TimestampPrecisionRange(Closed(TimestampPrecisionValue.Day), Unbounded)) to "timestamp_precision: range::[day, max]",
    ),
)
