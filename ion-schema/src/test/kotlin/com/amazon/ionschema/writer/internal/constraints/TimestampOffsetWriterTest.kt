// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.writer.internal.constraints

import com.amazon.ionschema.model.Constraint.TimestampOffset
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TimestampOffsetValue.Companion.parse

@OptIn(ExperimentalIonSchemaModel::class)
class TimestampOffsetWriterTest : ConstraintTestBase(
    writer = TimestampOffsetWriter,
    expectedConstraints = setOf(TimestampOffset::class),
    writeTestCases = listOf(
        TimestampOffset(emptySet()) to "timestamp_offset: []",
        TimestampOffset(setOf(parse("+01:23"))) to """ timestamp_offset: ["+01:23"] """,
        TimestampOffset(setOf(parse("+01:23"), parse("-04:56"))) to """ timestamp_offset: ["+01:23", "-04:56"] """,
    )
)
