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

import com.amazon.ion.IonSymbol
import com.amazon.ionschema.InvalidSchemaException
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DeferredReferenceManagerTest {

    private val loadSchema: (DeferredReferenceManager, String) -> SchemaInternal = mockk(name = "loadSchema")
    private val unloadSchema: (String) -> Unit = mockk(name = "unloadSchema")
    private val isSchemaAlreadyLoaded: (String) -> Boolean = mockk(name = "isSchemaAlreadyLoaded")
    private val doesSchemaDeclareType: (String, IonSymbol) -> Boolean = mockk(name = "doesSchemaDeclareType") {
        every { this@mockk(any(), any()) } returns true
    }

    private val typeId = mockk<IonSymbol> { every { stringValue() } returns "typeId" }
    private val schemaId = "schemaId"

    private fun mockSchema(containedType: TypeInternal?) = mockk<SchemaInternal> {
        every { this@mockk.schemaId } returns this@DeferredReferenceManagerTest.schemaId
        every { getType("typeId") } returns containedType
    }

    @AfterEach
    fun cleanup() = clearMocks(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)

    @Test
    fun `create and resolve an DeferredImportReference`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        val loadedSchema = mockSchema(containedType = mockk())
        every { loadSchema(drm, schemaId) } returns loadedSchema

        drm.createDeferredImportReference(schemaId, typeId)

        // This should not be called until we call drm.resolve()
        verify(exactly = 0) { loadSchema(drm, schemaId) }

        drm.resolve()

        verify { doesSchemaDeclareType(schemaId, typeId) }
        verify { loadSchema(drm, schemaId) }
        verify { loadedSchema.getType("typeId") }
    }

    @Test
    fun `attempting to create an DeferredImportReference for a type that is not declared should throw InvalidSchemaException`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        clearMocks(doesSchemaDeclareType)
        every { doesSchemaDeclareType(schemaId, typeId) } returns false

        assertThrows<InvalidSchemaException> {
            drm.createDeferredImportReference(schemaId, typeId)
        }

        verify { doesSchemaDeclareType(schemaId, typeId) }
    }

    @Test
    fun `create and resolve an DeferredLocalReference`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        val loadedSchema = mockSchema(containedType = mockk())

        drm.createDeferredLocalReference(loadedSchema, typeId)
        drm.resolve()

        verify { loadedSchema.getType("typeId") }
    }

    @Test
    fun `attempting to create an DeferredLocalReference for a type that is not declared should throw InvalidSchemaException`() {
        // Caveat—this does not apply to anonymous schemas for now.
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        val loadedSchema = mockSchema(containedType = mockk())
        clearMocks(doesSchemaDeclareType)
        every { doesSchemaDeclareType(schemaId, typeId) } returns false

        assertThrows<InvalidSchemaException> {
            drm.createDeferredLocalReference(loadedSchema, typeId)
        }

        verify { doesSchemaDeclareType(schemaId, typeId) }
    }

    @Test
    fun `registerDependentSchema() should do nothing with a schemaId that is already loaded`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        every { isSchemaAlreadyLoaded("foo") } returns true

        drm.registerDependentSchema("foo")

        verify(exactly = 1) { isSchemaAlreadyLoaded("foo") }
    }

    @Test
    fun `registerDependentSchema() should save a schemaId that is not already loaded`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        every { isSchemaAlreadyLoaded("foo") } returns false
        every { loadSchema(drm, "foo") } returns mockk()

        drm.registerDependentSchema("foo")
        drm.resolve()

        verify(exactly = 1) { isSchemaAlreadyLoaded("foo") }
    }

    @Test
    fun `when any reference cannot be resolved, it should throw InvalidSchemaException on close`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        val mockSchema = mockSchema(containedType = null)

        drm.createDeferredLocalReference(mockSchema, typeId)
        assertThrows<InvalidSchemaException> { drm.resolve() }
    }

    @Test
    fun `when the target schema of an imported reference cannot be loaded, it should throw InvalidSchemaException on close`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)

        every { loadSchema(drm, "schemaId") } throws InvalidSchemaException("Oh no!")

        drm.createDeferredImportReference("schemaId", typeId)
        assertThrows<InvalidSchemaException> { drm.resolve() }
    }

    @Test
    fun `when any reference is unresolvable, it should invalidate all dependent schemas on close`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        val mockSchema = mockSchema(containedType = null)

        every { isSchemaAlreadyLoaded(any()) } returns false
        every { unloadSchema(any()) } returns Unit

        drm.registerDependentSchema("foo")
        drm.registerDependentSchema("bar")
        drm.createDeferredLocalReference(mockSchema, typeId)
        assertThrows<InvalidSchemaException> { drm.resolve() }

        verify(exactly = 1) { unloadSchema("bar") }
        verify(exactly = 1) { unloadSchema("foo") }
    }

    @Test
    fun `when already closed, calling createDeferredLocalReference() should throw an exception`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        drm.resolve()
        assertThrows<IllegalStateException> { drm.createDeferredLocalReference(mockk(), mockk()) }
    }

    @Test
    fun `when already closed, calling createDeferredImportReference() should throw an exception`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        drm.resolve()
        assertThrows<IllegalStateException> { drm.createDeferredImportReference("schemaId", mockk()) }
    }

    @Test
    fun `when already closed, calling registerDependentSchema() should throw an exception`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        drm.resolve()
        assertThrows<IllegalStateException> { drm.registerDependentSchema("schemaId") }
    }

    @Test
    fun `when already closed, calling resolve() should throw an exception`() {
        val drm = DeferredReferenceManager(loadSchema, unloadSchema, isSchemaAlreadyLoaded, doesSchemaDeclareType)
        drm.resolve()
        assertThrows<IllegalStateException> { drm.resolve() }
    }
}
