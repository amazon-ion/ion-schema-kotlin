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

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaException
import com.amazon.ionschema.internal.util.RangeBoundaryType.INCLUSIVE
import com.amazon.ionschema.internal.util.RangeBoundaryType.EXCLUSIVE

internal class RangeBoundaryTypeTest {
    private val ION = IonSystemBuilder.standard().build()

    @Test fun exclusive()     { assert(EXCLUSIVE, "exclusive::5") }
    @Test fun inclusive()     { assert(INCLUSIVE, "min") }
    @Test fun max()           { assert(INCLUSIVE, "max") }
    @Test fun min()           { assert(INCLUSIVE, "min") }
    @Test fun max_exclusive() { assertException("exclusive::max") }
    @Test fun min_exclusive() { assertException("exclusive::min") }

    private fun assert(expected: RangeBoundaryType, str: String) {
        assertEquals(expected, RangeBoundaryType.forIon(ION.singleValue(str)))
    }

    private fun assertException(str: String) {
        try {
            RangeBoundaryType.forIon(ION.singleValue(str))
            fail("Expected an IonSchemaException")
        } catch (e: IonSchemaException) {
        }
    }
}

