// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.ConsistentDecimal
import com.amazon.ionschema.model.ConsistentTimestamp
import com.amazon.ionschema.model.Constraint.ValidValues
import com.amazon.ionschema.model.ContinuousRange.Limit.Closed
import com.amazon.ionschema.model.ContinuousRange.Limit.Open
import com.amazon.ionschema.model.ContinuousRange.Limit.Unbounded
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.ValidValue.NumberRange
import com.amazon.ionschema.model.ValidValue.TimestampRange
import com.amazon.ionschema.model.ValidValue.Value
import java.math.BigDecimal

@OptIn(ExperimentalIonSchemaModel::class)
class ValidValuesWriterTest : ConstraintTestBase(
    writer = ValidValuesWriter,
    expectedConstraints = setOf(ValidValues::class),
    writeTestCases = listOf(
        ValidValues(emptySet()) to "valid_values: []",
        ValidValues(Value(ion("a"))) to "valid_values: [a]",
        ValidValues(Value(ion("a")), Value(ion("b"))) to "valid_values: [a, b]",
        // Number ranges
        ValidValues(NumberRange(ConsistentDecimal(BigDecimal.ZERO))) to "valid_values: [range::[0., 0.]]",
        ValidValues(NumberRange(Open(ConsistentDecimal.valueOf(1L)), Closed(ConsistentDecimal.valueOf(3L)))) to "valid_values: [range::[exclusive::1., 3.]]",
        ValidValues(NumberRange(Closed(ConsistentDecimal.valueOf(1.0)), Unbounded)) to "valid_values: [range::[1.0, max]]",
        ValidValues(NumberRange(Unbounded, Closed(ConsistentDecimal.valueOf(3L)))) to "valid_values: [range::[min, 3.]]",
        // Timestamp ranges
        ValidValues(
            TimestampRange(
                ConsistentTimestamp.valueOf("2023-01-01T00:00:00Z")
            )
        ) to "valid_values: [range::[2023-01-01T00:00:00Z, 2023-01-01T00:00:00Z]]",
        ValidValues(
            TimestampRange(
                Open(ConsistentTimestamp.valueOf("2023-01-02T00:00:00Z")),
                Closed(ConsistentTimestamp.valueOf("2023-01-04T00:00:00Z")),
            )
        ) to "valid_values: [range::[exclusive::2023-01-02T00:00:00Z, 2023-01-04T00:00:00Z]]",
        ValidValues(
            TimestampRange(
                Closed(ConsistentTimestamp.valueOf("2023-01-05T00:00:00Z")),
                Unbounded
            )
        ) to "valid_values: [range::[2023-01-05T00:00:00Z, max]]",
        ValidValues(
            TimestampRange(
                Unbounded,
                Closed(ConsistentTimestamp.valueOf("2023-01-06T00:00:00Z"))
            )
        ) to "valid_values: [range::[min, 2023-01-06T00:00:00Z]]",
    )
)
