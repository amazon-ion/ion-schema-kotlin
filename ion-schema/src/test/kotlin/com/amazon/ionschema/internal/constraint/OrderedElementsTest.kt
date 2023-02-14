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
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class OrderedElementsTest {
    private val ISS = IonSchemaSystemBuilder.standard().build() as IonSchemaSystemImpl
    val ION = ISS.ionSystem

    @ParameterizedTest(name = "ordered_elements:{0} should {1} {2}")
    @MethodSource("testCases")
    fun test(ionText: String, ignored: String, value: String, expectValid: Boolean) {
        val constraint = ISS.usingReferenceManager { OrderedElements(ION.singleValue(ionText), ISS.newSchema(), it) }
        val v = Violations()
        constraint.validate(ION.singleValue(value), v, debug = true)
        assertEquals(expectValid, v.isValid())
    }

    @ParameterizedTest(name = "violations message - {0}")
    @MethodSource("messageTestCases")
    fun test_messages(_name: String, constraintIon: String, value: String, message: String) {
        val constraint = ISS.usingReferenceManager { OrderedElements(ION.singleValue(constraintIon), ISS.newSchema(), it) }
        val v = Violations()
        constraint.validate(ION.singleValue(value), v, debug = true)
        // Trim all whitespace to normalize before comparing
        val actual = v.toString().trim()
        val expected = message.trim()
        // We add a newline to make the assertion message easier to read
        assertEquals("\n$expected\n", "\n$actual\n")
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

        private fun messageTestCase(name: String, orderedElements: String, value: String, message: String) =
            Arguments { arrayOf(name, orderedElements, value, message.trimIndent()) }

        @JvmStatic
        fun messageTestCases(): Iterable<Arguments> = listOf(
            messageTestCase(
                name = "Value does not match type",
                orderedElements = "[int]",
                value = "(foo)",
                message = """
                Validation failed:
                - one or more ordered elements don't match specification
                  - [0]: foo
                    - does not match: <ELEMENT 0> int
                """
            ),
            messageTestCase(
                name = "End of sexp when another type is expected",
                orderedElements = "[symbol, symbol]",
                value = "(foo)",
                message = """
                Validation failed:
                - one or more ordered elements don't match specification
                  - <END OF SEXP>
                    - does not match: <ELEMENT 1> symbol
                """
            ),
            messageTestCase(
                name = "End of list when another type is expected",
                orderedElements = "[symbol, symbol]",
                value = "[foo]",
                message = """
                Validation failed:
                - one or more ordered elements don't match specification
                  - <END OF LIST>
                    - does not match: <ELEMENT 1> symbol
                """
            ),
            messageTestCase(
                name = "Value does not match end of sexp",
                orderedElements = "[symbol]",
                value = "(foo bar)",
                message = """
                Validation failed:
                - one or more ordered elements don't match specification
                  - [1]: bar
                    - does not match: <END OF SEXP>
                """
            ),
            messageTestCase(
                name = "Value does not match end of list",
                orderedElements = "[symbol]",
                value = "[foo, bar]",
                message = """
                Validation failed:
                - one or more ordered elements don't match specification
                  - [1]: bar
                    - does not match: <END OF LIST>
                """
            ),
            messageTestCase(
                name = "Value does not match any of multiple types",
                orderedElements = "[symbol, {occurs:optional,type:int}, {occurs:optional,type:bool}, {occurs:optional,type:int}]",
                value = "(foo bar)",
                message = """
                Validation failed:
                - one or more ordered elements don't match specification
                  - [1]: bar
                    - does not match: <ELEMENT 1> {occurs:optional,type:int}
                      - expected type int, found symbol
                    - does not match: <ELEMENT 2> {occurs:optional,type:bool}
                      - expected type bool, found symbol
                    - does not match: <ELEMENT 3> {occurs:optional,type:int}
                      - expected type int, found symbol
                    - does not match: <END OF SEXP>
                """
            ),
            messageTestCase(
                name = "Min occurs not met",
                orderedElements = "[{occurs:range::[2,3], type:symbol}]",
                value = "(foo)",
                message = """
                Validation failed:
                - one or more ordered elements don't match specification
                  - <END OF SEXP>
                    - does not match: <ELEMENT 0> {occurs:range::[2,3],type:symbol}
                    - min occurs not reached: <ELEMENT 0> {occurs:range::[2,3],type:symbol}
                """
            ),
            messageTestCase(
                name = "Max occurs exceeded",
                orderedElements = "[{occurs:2, type:symbol}]",
                value = "(foo bar baz)",
                message = """
                Validation failed:
                - one or more ordered elements don't match specification
                  - [2]: baz
                    - max occurs already reached: <ELEMENT 0> {occurs:2,type:symbol}
                    - does not match: <END OF SEXP>
                """
            ),
        )
    }
}
