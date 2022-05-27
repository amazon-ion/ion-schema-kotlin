/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.ionschema.model.constraints

import com.amazon.ionschema.model.*

data class TimestampPrecisionConstraint(val range: TimestampPrecisionRange) : AstConstraint<TimestampPrecisionConstraint> {
    companion object : ConstraintId<TimestampPrecisionConstraint> by ConstraintId("timestamp_precision") {
        @JvmField val ID = this@Companion
    }
    override val id get() = ID
}

class TimestampPrecisionRange private constructor(private val delegate: RangeDelegate<TimestampPrecisions>) : Range<TimestampPrecisions> by delegate {
    constructor(min: Min, max: TimestampPrecisions) : this(RangeDelegate(min, max))
    constructor(min: TimestampPrecisions, max: TimestampPrecisions) : this(RangeDelegate(min, max))
    constructor(min: TimestampPrecisions, max: Max) : this(RangeDelegate(min, max))

    init {
        if (delegate.min is TimestampPrecisions && delegate.max is TimestampPrecisions) require(delegate.min <= delegate.max)
    }

    override fun hashCode(): Int = delegate.hashCode()
    override fun equals(other: Any?): Boolean = other is TimestampPrecisionRange && delegate == other.delegate
    override fun toString(): String = "TimestampPrecisionRange(min=$min,max=$max)"
}

enum class TimestampPrecisions {
    Year,
    Month,
    Day,
    Minute,
    Second,
    Millisecond,
    Microsecond,
    Nanosecond,
}
