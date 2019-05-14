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

import com.amazon.ion.IonList
import com.amazon.ionschema.InvalidSchemaException

/**
 * Implementation of Range<Int> restricted to non-negative integers.
 * Mostly delegates to RangeInt.
 */
internal class RangeIntNonNegative (
        private val ion: IonList,
        private val delegate: RangeInt = RangeInt(ion)
) : Range<Int> by delegate {

    init {
        if (!(compareValues(toInt(ion[0]), 0) >= 0 || isRangeMin(ion[0]))) {
            throw InvalidSchemaException("Invalid lower bound in positive int $ion")
        }

        if (!(compareValues(toInt(ion[1]), 0) >= 0 || isRangeMax(ion[1]))) {
            throw InvalidSchemaException("Invalid upper bound in positive int $ion")
        }
    }

    internal fun isAtMax(value: Int) = delegate.isAtMax(value)

    override fun toString() = delegate.toString()
}

