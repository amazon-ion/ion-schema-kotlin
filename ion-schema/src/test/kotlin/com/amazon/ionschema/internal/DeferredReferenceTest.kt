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
        fun typeNotFound() {
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
        fun typeNotFound() {
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
    }
}
