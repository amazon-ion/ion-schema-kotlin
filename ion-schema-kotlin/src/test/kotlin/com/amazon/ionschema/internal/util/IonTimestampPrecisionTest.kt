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

import com.amazon.ion.IonTimestamp
import com.amazon.ion.system.IonSystemBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class IonTimestampPrecisionTest {
    private val ION = IonSystemBuilder.standard().build()

    @Test
    fun timestamp_precisions_valid() {
        assert(IonTimestampPrecision.year.id, "2000T")
        assert(IonTimestampPrecision.month.id, "2000-01T")
        assert(IonTimestampPrecision.day.id, "2000-01-01T")
        // hour without minute is not valid Ion
        assert(IonTimestampPrecision.minute.id, "2000-01-01T00:00Z")
        assert(IonTimestampPrecision.second.id, "2000-01-01T00:00:00Z")
        assert(1, "2000-01-01T00:00:00.0Z")
        assert(2, "2000-01-01T00:00:00.00Z")
        assert(IonTimestampPrecision.millisecond.id, "2000-01-01T00:00:00.000Z")
        assert(4, "2000-01-01T00:00:00.0000Z")
        assert(5, "2000-01-01T00:00:00.00000Z")
        assert(IonTimestampPrecision.microsecond.id, "2000-01-01T00:00:00.000000Z")
        assert(7, "2000-01-01T00:00:00.0000000Z")
        assert(8, "2000-01-01T00:00:00.00000000Z")
        assert(IonTimestampPrecision.nanosecond.id, "2000-01-01T00:00:00.000000000Z")
    }

    @Test
    fun timestamp_precisions_invalid() {
        assertInvalid("2000-01-01T00Z")
    }

    private fun assert(expected: Int, value: String) {
        assertEquals(
            expected,
            IonTimestampPrecision.toInt(ION.singleValue(value) as IonTimestamp)
        )
    }

    private fun assertInvalid(value: String) {
        try {
            IonTimestampPrecision.toInt(ION.singleValue(value) as IonTimestamp)
            fail("Expected an exception from $value")
        } catch (e: RuntimeException) {
        }
    }
}
