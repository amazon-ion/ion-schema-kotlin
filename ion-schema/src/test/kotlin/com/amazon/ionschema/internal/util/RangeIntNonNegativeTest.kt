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
import org.junit.jupiter.api.Test

internal class RangeIntNonNegativeTest : AbstractRangeTest(RangeType.INT_NON_NEGATIVE) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> rangeOf(ion: IonList) = RangeFactory.rangeOf<Int>(ion, RangeType.INT_NON_NEGATIVE) as Range<T>

    @Test
    fun range_int_inclusive() {
        assertValidRangeAndValues(
            "range::[0, 100]",
            listOf(0, 100),
            listOf(-1, 101)
        )
    }

    @Test
    fun range_int_exclusive() {
        assertValidRangeAndValues(
            "range::[exclusive::0, exclusive::100]",
            listOf(1, 99),
            listOf(0, 100)
        )
    }

    @Test
    fun range_invalid() {
        assertInvalidRange("range::[-1, 0]")
        assertInvalidRange("range::[0, -1]")

        assertInvalidRange("range::[exclusive::1, 1]")
        assertInvalidRange("range::[1, exclusive::1]")

        assertInvalidRange("range::[0, exclusive::1]")
        assertInvalidRange("range::[exclusive::0, 1]")

        assertInvalidRange("range::[0d0, 1]")
        assertInvalidRange("range::[0, 1d0]")
        assertInvalidRange("range::[0e0, 1]")
        assertInvalidRange("range::[0, 1e0]")
    }
}
