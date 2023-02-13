/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.ionschema.internal

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SchemaContentCacheTest {

    private val ION = IonSystemBuilder.standard().build()
    private val TYPE_ID_1 = ION.newSymbol("1")
    private val TYPE_ID_2 = ION.newSymbol("2")

    private fun defaultDelegateValue() = mutableMapOf("a" to schemaContentMockA)

    private val loaderMock = mockk<(String) -> SchemaContent>()
    private val schemaContentMockA = mockk<SchemaContent>()
    private val schemaContentMockB = mockk<SchemaContent>()
    private lateinit var delegate: MutableMap<String, SchemaContent>
    private lateinit var scc: SchemaContentCache

    @BeforeEach
    fun setup() {
        delegate = defaultDelegateValue()
        scc = SchemaContentCache(loaderMock, delegate)
        every { loaderMock.invoke("b") } returns schemaContentMockB
        every { loaderMock.invoke("c") } throws Exception("Oh no!")
    }

    @AfterEach
    fun cleanup() {
        confirmVerified(loaderMock)
        clearMocks(loaderMock, schemaContentMockA, schemaContentMockB)
    }

    @Test
    fun `invalidate() should remove any corresponding entry from the backing map`() {
        scc.invalidate("a")

        assertEquals(emptyMap<String, SchemaContent>(), delegate, "The SchemaContent should have been removed")
    }

    @Test
    fun `invalidate() should do nothing if nothing is cached for that schemaId`() {
        scc.invalidate("b")

        assertEquals(defaultDelegateValue(), delegate, "The backing map should be unchanged")
    }

    @Test
    fun `getSchemaContent() should return an already-cached SchemaContent`() {
        val result = scc.getSchemaContent("a")

        assertEquals(schemaContentMockA, result, "The SchemaContentCache should return the expected SchemaContent")
    }

    @Test
    fun `getSchemaContent() should fetch and cache a not-yet cached SchemaContent`() {
        val result = scc.getSchemaContent("b")

        assertEquals(schemaContentMockB, result, "The SchemaContentCache should return the expected SchemaContent")
        assertEquals(mapOf("a" to schemaContentMockA, "b" to schemaContentMockB), delegate, "The fetched SchemaContent should be cached")
        verify(exactly = 1) { loaderMock.invoke("b") }
    }

    @Test
    fun `getSchemaContent() should wrap and rethrow any exceptions`() {
        assertThrows<InvalidSchemaException> {
            scc.getSchemaContent("c")
        }
        assertEquals(defaultDelegateValue(), delegate, "The backing map should be unchanged")
        verify(exactly = 1) { loaderMock.invoke("c") }
    }

    @Test
    fun `doesSchemaExist() should check the backing map before attempting to load and return true if successful`() {
        val doesSchemaExist = scc.doesSchemaExist("a")

        assertTrue(doesSchemaExist)
        assertEquals(defaultDelegateValue(), delegate, "The backing map should be unchanged")
    }

    @Test
    fun `doesSchemaExist() should attempt to load SchemaContent and return true if successful`() {
        val doesSchemaExist = scc.doesSchemaExist("b")

        assertTrue(doesSchemaExist)
        assertEquals(mapOf("a" to schemaContentMockA, "b" to schemaContentMockB), delegate, "The fetched SchemaContent should be cached")
        verify(exactly = 1) { loaderMock.invoke("b") }
    }

    @Test
    fun `doesSchemaExist() should attempt to load SchemaContent and return false if an exception is thrown`() {
        val doesSchemaExist = scc.doesSchemaExist("c")

        assertFalse(doesSchemaExist)
        assertEquals(defaultDelegateValue(), delegate, "The backing map should be unchanged")
        verify(exactly = 1) { loaderMock.invoke("c") }
    }

    @Test
    fun `doesSchemaDeclareType() should return true if type is declared`() {
        every { schemaContentMockA.declaredTypes } returns listOf(TYPE_ID_1)

        val doesSchemaDeclareType = scc.doesSchemaDeclareType("a", TYPE_ID_1)

        assertTrue(doesSchemaDeclareType)
        assertEquals(defaultDelegateValue(), delegate, "The backing map should be unchanged")
    }

    @Test
    fun `doesSchemaDeclareType() should load SchemaContent if not in backing map`() {
        every { schemaContentMockB.declaredTypes } returns listOf(TYPE_ID_1)

        val doesSchemaDeclareType = scc.doesSchemaDeclareType("b", TYPE_ID_1)

        assertTrue(doesSchemaDeclareType)
        assertEquals(mapOf("a" to schemaContentMockA, "b" to schemaContentMockB), delegate, "The fetched SchemaContent should be cached")
        verify(exactly = 1) { loaderMock.invoke("b") }
    }

    @Test
    fun `doesSchemaDeclareType() should return false if type is not declared`() {
        every { schemaContentMockB.declaredTypes } returns listOf(TYPE_ID_1)

        val doesSchemaDeclareType = scc.doesSchemaDeclareType("b", TYPE_ID_2)

        assertFalse(doesSchemaDeclareType)
        assertEquals(mapOf("a" to schemaContentMockA, "b" to schemaContentMockB), delegate, "The fetched SchemaContent should be cached")
        verify(exactly = 1) { loaderMock.invoke("b") }
    }

    @Test
    fun `doesSchemaDeclareType() should return false if an exception is thrown`() {
        val doesSchemaDeclareType = scc.doesSchemaDeclareType("c", TYPE_ID_1)

        assertFalse(doesSchemaDeclareType)
        assertEquals(defaultDelegateValue(), delegate, "The backing map should be unchanged")
        verify(exactly = 1) { loaderMock.invoke("c") }
    }
}
