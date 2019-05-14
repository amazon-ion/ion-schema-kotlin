/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.ionschema.internal.util

import com.amazon.ion.IonInt
import com.amazon.ion.IonList
import com.amazon.ionschema.InvalidSchemaException
import java.math.BigDecimal

/**
 * Implementation of Range<Int>, which mostly delegates to RangeBigDecimal.
 */
internal class RangeInt (
        private val ion: IonList,
        private val delegate: RangeBigDecimal = RangeBigDecimal(ion)
) : Range<Int> {

    init {
        if (!(ion[0] is IonInt || isRangeMin(ion[0]))) {
            throw InvalidSchemaException("Invalid lower bound in int $ion")
        }

        if (!(ion[1] is IonInt || isRangeMax(ion[1]))) {
            throw InvalidSchemaException("Invalid upper bound in int $ion")
        }

        if (delegate.lower.value != null && delegate.upper.value != null
                && (delegate.lower.boundaryType == RangeBoundaryType.EXCLUSIVE
                    || delegate.upper.boundaryType == RangeBoundaryType.EXCLUSIVE)) {
            val minPlusOne = delegate.lower.value.add(BigDecimal.ONE)
            if (minPlusOne == delegate.upper.value) {
                throw InvalidSchemaException("No valid values in the int range $ion")
            }
        }
    }

    override fun contains(value: Int) = delegate.contains(value.toBigDecimal())

    internal fun isAtMax(value: Int) = delegate.upper.compareTo(value.toBigDecimal()) == 0

    override fun toString() = ion.toString()
}

