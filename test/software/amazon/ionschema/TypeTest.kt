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

package software.amazon.ionschema

import org.junit.Assert.*
import org.junit.Test
import software.amazon.ion.system.IonSystemBuilder

class TypeTest {
    private val ION = IonSystemBuilder.standard().build()

    private val type = IonSchemaSystemBuilder.standard()
            .build()
            .newSchema()
            .newType("type::{ name: a, type: string }")

    @Test
    fun name() = assertEquals("a", type.name)

    @Test
    fun isValid_true() = assertTrue(type.isValid(ION.singleValue("\"hello\"")))

    @Test
    fun isValid_false() = assertFalse(type.isValid(ION.singleValue("1")))

    @Test(expected = NoSuchElementException::class)
    fun validate_success() {
        val violations = type.validate(ION.singleValue("\"hello\""))
        assertNotNull(violations)
        assertTrue(violations.isValid())
        assertFalse(violations.iterator().hasNext())
        violations.iterator().next()
    }

    @Test
    fun validate_violations() {
        val violations = type.validate(ION.singleValue("1"))
        assertNotNull(violations)
        assertFalse(violations.isValid())
        assertTrue(violations.iterator().hasNext())

        val iter = violations.iterator()
        val violation = iter.next()
        assertEquals("type_mismatch", violation.code)
        assertEquals("type", violation.constraint?.fieldName)
        assertEquals(ION.singleValue("string"), violation.constraint)
    }
}

