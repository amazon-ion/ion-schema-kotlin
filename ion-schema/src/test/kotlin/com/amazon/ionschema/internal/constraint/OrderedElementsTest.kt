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

package com.amazon.ionschema.internal.constraint

import com.amazon.ionschema.IonSchemaSystemBuilder
import com.amazon.ionschema.Violations
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class OrderedElementsTest {
    val ISS = IonSchemaSystemBuilder.standard().build()
    val ION = ISS.ionSystem

    @ParameterizedTest(name = "ordered_elements:{0} should {1} {2}")
    @MethodSource("testCases")
    fun test(ionText: String, ignored: String, value: String, expectValid: Boolean) {
        val constraint = OrderedElements(ION.singleValue(ionText), ISS.newSchema())
        val v = Violations()
        constraint.validate(ION.singleValue(value), v, debug = true)
        assertEquals(expectValid, v.isValid())
    }

    companion object {
        private val testCases = mapOf(
            "[{occurs:optional, type:int},{occurs:optional, type:number}]" to values(
                valid = listOf("[]", "[1]", "[1.0]", "[1, 2]", "[1, 2.0]"),
                invalid = listOf("[1.0, 2]", "[1, 2, 3]")
            ),
            "[{occurs:optional, type:int}, number]" to values(
                valid = listOf("[1]", "[1.0]", "[1, 2]", "[1, 2.0]"),
                invalid = listOf("[]", "[1.0, 2]", "[1, 2, 3]")
            ),

            "[int, {occurs:optional, type:number}]" to values(
                valid = listOf("[1]", "[1, 2]", "[1, 2.0]"),
                invalid = listOf("[]", "[1.0]", "[1.0, 2]", "[1, 2, 3]")
            ),
        )

        private fun values(valid: List<String>, invalid: List<String>) = Pair(valid, invalid)

        @JvmStatic
        fun testCases(): Iterable<Arguments> = testCases.entries.flatMap { (orderedElements, values) ->
            val valid = values.first.map { Arguments { arrayOf(orderedElements, "accept", it, true) } }
            val invalid = values.second.map { Arguments { arrayOf(orderedElements, "reject", it, false) } }
            valid + invalid
        }
    }
}
