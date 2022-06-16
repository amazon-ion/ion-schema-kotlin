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
import com.amazon.ion.IonValue
import org.junit.jupiter.api.Test

internal class RangeIonNumberTest : AbstractRangeTest(RangeType.ION_NUMBER) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> rangeOf(ion: IonList) = RangeFactory.rangeOf<IonValue>(ion, RangeType.ION_NUMBER) as Range<T>

    @Test
    fun range_decimal_inclusive() {
        assertValidRangeAndValues(
            "range::[-100d0, 100d0]",
            ionListOf(
                "-100", "-100d0", "-100.00000000d0", "-100e0", "-100.00000000e0",
                "100", "100d0", "100.00000000d0", "100e0", "100.00000000e0"
            ),

            ionListOf(
                "-101", "-100.00000001d0", "-100.00000001e0",
                "101", "100.00000001d0", "100.00000001e0"
            )
        )
    }

    @Test
    fun range_decimal_exclusive() {
        assertValidRangeAndValues(
            "range::[exclusive::-100d0, exclusive::100d0]",
            ionListOf(
                "-99", "-99.99999999d0", "-99.99999999e0",
                "99", "99.99999999d0", "99.99999999e0"
            ),

            ionListOf(
                "-100", "-100d0", "-100.00000000d0", "-100e0", "-100.00000000e0",
                "100", "100d0", "100.00000000d0", "100e0", "100.00000000e0"
            )
        )
    }

    @Test
    fun range_float_inclusive() {
        assertValidRangeAndValues(
            "range::[-100e0, 100e0]",
            ionListOf(
                "-100", "-100d0", "-100.00000000d0", "-100e0", "-100.00000000e0",
                "100", "100d0", "100.00000000d0", "100e0", "100.00000000e0"
            ),

            ionListOf(
                "-101", "-100.00000001d0", "-100.00000001e0",
                "101", "100.00000001d0", "100.00000001e0"
            )
        )
    }

    @Test
    fun range_float_exclusive() {
        assertValidRangeAndValues(
            "range::[exclusive::-100e0, exclusive::100e0]",
            ionListOf(
                "-99", "-99.99999999d0", "-99.99999999e0",
                "99", "99.99999999d0", "99.99999999e0"
            ),

            ionListOf(
                "-100", "-100d0", "-100.00000000d0", "-100e0", "-100.00000000e0",
                "100", "100d0", "100.00000000d0", "100e0", "100.00000000e0"
            )
        )
    }

    @Test
    fun range_int_inclusive() {
        assertValidRangeAndValues(
            "range::[-100, 100]",
            ionListOf("-100", "0", "100"),
            ionListOf("-101", "101")
        )
    }

    @Test
    fun range_int_exclusive() {
        assertValidRangeAndValues(
            "range::[exclusive::-100,exclusive::100]",
            ionListOf("-99", "0", "99"),
            ionListOf("-100", "100")
        )
    }

    @Test
    fun range_invalid() {
        assertInvalidRange("range::[1, min]")
        assertInvalidRange("range::[max, 1]")
        assertInvalidRange("range::[max, min]")

        assertInvalidRange("range::[exclusive::1, 1]")
        assertInvalidRange("range::[1, exclusive::1]")

        assertInvalidRange("range::[exclusive::2d0, 2d0]")
        assertInvalidRange("range::[2d0, exclusive::2d0]")

        assertInvalidRange("range::[exclusive::3e0, 3e0]")
        assertInvalidRange("range::[3e0, exclusive::3e0]")

        assertInvalidRange("range::[1, 0]")
        assertInvalidRange("range::[1.00000000d0, 0.99999999d0]")
        assertInvalidRange("range::[1.00000000e0, 0.99999999e0]")
    }
}
