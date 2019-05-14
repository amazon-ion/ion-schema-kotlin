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

import org.junit.Assert
import org.junit.Assert.fail
import com.amazon.ion.IonList
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException

internal abstract class AbstractRangeTest(
        private val rangeType: RangeType
) {
    private val ION = IonSystemBuilder.standard().build()

    abstract fun <T : Any> rangeOf(ion: IonList): Range<T>

    fun assertValidRangeAndValues(
            rangeDef: String,
            validValues: List<Any>,
            invalidValues: List<Any>) {

        val range = rangeOf<Any>(ION.singleValue(rangeDef) as IonList)
        validValues.forEach {
            Assert.assertTrue("Expected $it to be within $rangeDef",
                    range.contains(it))
        }
        invalidValues.forEach {
            Assert.assertFalse("Didn't expect $it to be within $rangeDef",
                    range.contains(it))
        }
    }

    fun assertInvalidRange(rangeDef: String) {
        try {
            rangeOf<Any>(ION.singleValue(rangeDef) as IonList)
            fail("Expected InvalidSchemaException for $rangeDef")
        } catch (e: InvalidSchemaException) {
        }
    }

    fun ionListOf(vararg items: String): IonList {
        val ionList = ION.newEmptyList()
        items.forEach {
            ionList.add(ION.singleValue(it))
        }
        return ionList
    }
}

