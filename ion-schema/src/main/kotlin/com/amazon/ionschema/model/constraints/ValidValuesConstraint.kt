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

import com.amazon.ionelement.api.IonElement
import com.amazon.ionschema.model.*
import java.math.BigDecimal
import java.time.Instant

data class ValidValuesConstraint(val values: Iterable<ValidValue>) : AstConstraint<ValidValuesConstraint> {
    companion object : ConstraintId<ValidValuesConstraint> by ConstraintId("valid_values") {
        @JvmField val ID = this@Companion
    }
    override val id get() = ID
}

sealed class ValidValue {
    data class SingleValue(val value: IonElement) : ValidValue() {
        init { require(value.annotations.isEmpty()) }
    }
    class TimestampRange private constructor(private val delegate: RangeDelegate<Instant>) : ValidValue(), Range<Instant> by delegate {
        constructor(min: Boundary<Instant>, max: Boundary<Instant>) : this(RangeDelegate(min, max))
        constructor(min: Instant, max: Boundary<Instant>) : this(RangeDelegate(min, max))
        constructor(min: Boundary<Instant>, max: Instant) : this(RangeDelegate(min, max))
        constructor(min: Instant, max: Instant) : this(RangeDelegate(min, max))

        override fun hashCode(): Int = delegate.hashCode()
        override fun equals(other: Any?): Boolean = other is TimestampRange && delegate == other.delegate
        override fun toString(): String = "TimestampRange(min=$min,max=$max)"
    }
    class NumberRange private constructor(private val delegate: RangeDelegate<BigDecimal>) : ValidValue(), Range<BigDecimal> by delegate {
        constructor(min: Boundary<BigDecimal>, max: Boundary<BigDecimal>) : this(RangeDelegate(min, max))
        constructor(min: BigDecimal, max: Boundary<BigDecimal>) : this(RangeDelegate(min, max))
        constructor(min: Boundary<BigDecimal>, max: BigDecimal) : this(RangeDelegate(min, max))
        constructor(min: BigDecimal, max: BigDecimal) : this(RangeDelegate(min, max))

        override fun hashCode(): Int = delegate.hashCode()
        override fun equals(other: Any?): Boolean = other is NumberRange && delegate == other.delegate
        override fun toString(): String = "NumberRange(min=$min,max=$max)"
    }
}
