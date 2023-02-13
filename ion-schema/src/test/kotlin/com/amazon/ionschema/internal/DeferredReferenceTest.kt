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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

// Opting in to DeferredReferenceManagerImplementationDetails is okay here because we're testing the DeferredReferences.
@OptIn(DeferredReferenceManagerImplementationDetails::class)
class DeferredReferenceTest {

    val typeId = "typeId"
    val typeIdSymbol = IonSystemBuilder.standard().build().newSymbol(typeId)

    @Nested
    inner class DeferredLocalReferenceTest {

        @Test
        fun resolve() {
            val typeMock = mockk<TypeInternal>()
            val schemaMock = mockk<SchemaInternal> {
                every { getType(typeId) } returns typeMock
                every { schemaId } returns "schemaId"
            }

            val type = DeferredLocalReference(
                typeIdSymbol,
                schemaMock
            )

            assertEquals(typeMock, type.resolve())
            assertEquals(typeMock, type.resolve())

            verify(exactly = 1) { schemaMock.getType(typeId) }
        }

        @Test
        fun isResolved() {
            val schemaMock = mockk<SchemaInternal> {
                every { getType(typeId) } returns mockk<TypeInternal>()
                every { schemaId } returns "schemaId"
            }

            val type = DeferredLocalReference(
                typeIdSymbol,
                schemaMock
            )

            assertFalse(type.isResolved())
            type.resolve()
            assertTrue(type.isResolved())
        }

        @Test
        fun `when resolving the type fails, should throw InvalidSchemaException`() {
            val schemaMock = mockk<SchemaInternal> {
                every { getType(typeId) } returns null
                every { schemaId } returns "schemaId"
            }

            val type = DeferredLocalReference(
                typeIdSymbol,
                schemaMock
            )

            assertThrows<InvalidSchemaException> { type.resolve() }
        }

        @Test
        fun `using validation functions before resolving should throw IllegalStateException`() {
            val schemaMock = mockk<SchemaInternal> {
                every { getType(typeId) } returns mockk<TypeInternal>()
                every { schemaId } returns "schemaId"
            }

            val type = DeferredLocalReference(
                typeIdSymbol,
                schemaMock
            )

            assertThrows<IllegalStateException> { type.isValidForBaseType(mockk()) }
            assertThrows<IllegalStateException> { type.getBaseType() }
            assertThrows<IllegalStateException> { type.validate(mockk(), mockk()) }
        }
    }

    @Nested
    inner class DeferredImportReferenceTest {

        @Test
        fun resolve() {
            val typeMock = mockk<TypeInternal>()
            val schemaMock = mockk<SchemaInternal> {
                every { getType(typeId) } returns typeMock
            }
            val schemaProvider = mockk<() -> SchemaInternal>()
            every { schemaProvider.invoke() } returns schemaMock

            val type = DeferredImportReference(
                typeIdSymbol,
                "schemaId",
                schemaProvider
            )

            assertEquals(typeMock, type.resolve())
            assertEquals(typeMock, type.resolve())

            verify(exactly = 1) { schemaProvider.invoke() }
            verify(exactly = 1) { schemaMock.getType(typeId) }
        }

        @Test
        fun isResolved() {
            val schemaMock = mockk<SchemaInternal> {
                every { getType(typeId) } returns mockk<TypeInternal>()
            }

            val type = DeferredImportReference(
                typeIdSymbol,
                "schemaId",
                { schemaMock }
            )

            assertFalse(type.isResolved())
            type.resolve()
            assertTrue(type.isResolved())
        }

        @Test
        fun `when resolving the type fails, should throw InvalidSchemaException`() {
            val schemaMock = mockk<SchemaInternal> {
                every { getType(typeId) } returns null
            }

            val type = DeferredImportReference(
                typeIdSymbol,
                "schemaId",
                { schemaMock }
            )

            assertThrows<InvalidSchemaException> { type.resolve() }
        }

        @Test
        fun `using validation functions before resolving should throw IllegalStateException`() {
            val schemaMock = mockk<SchemaInternal> {
                every { getType(typeId) } returns mockk<TypeInternal>()
            }

            val type = DeferredImportReference(
                typeIdSymbol,
                "schemaId",
                { schemaMock }
            )

            assertThrows<IllegalStateException> { type.isValidForBaseType(mockk()) }
            assertThrows<IllegalStateException> { type.getBaseType() }
            assertThrows<IllegalStateException> { type.validate(mockk(), mockk()) }
        }
    }
}
