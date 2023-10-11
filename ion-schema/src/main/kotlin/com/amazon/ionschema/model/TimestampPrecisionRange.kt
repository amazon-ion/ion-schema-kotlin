// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.amazon.ionschema.model

/**
 * A [ContinuousRange] of [TimestampPrecisionValue].
 * `TimestampPrecision` is a discrete measurement (i.e. there is no fractional number of digits of precision).
 * However, because Ion Schema models timestamp precision as an enum, there are possible precisions that exist between
 * the available enum values. For example, `timestamp_precision: range::[exclusive::second, exclusive::millisecond]`
 * allows 1 or 2 digits of precision for the fractional seconds of a timestamp.
 */
class TimestampPrecisionRange(start: Limit<TimestampPrecisionValue>, end: Limit<TimestampPrecisionValue>) : ContinuousRange<TimestampPrecisionValue>(start, end) {
    private constructor(value: Limit.Closed<TimestampPrecisionValue>) : this(value, value)
    constructor(value: TimestampPrecisionValue) : this(Limit.Closed(value))
}
