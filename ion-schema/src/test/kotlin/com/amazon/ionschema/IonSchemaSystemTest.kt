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

package com.amazon.ionschema

import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaVersion.v1_0
import com.amazon.ionschema.internal.IonSchemaSystemImpl
import com.amazon.ionschema.util.CloseableIterator
import io.mockk.confirmVerified
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IonSchemaSystemTest {
    private class BadAuthorityException : Exception()

    private val exceptionalAuthority = object : Authority {
        internal var invokeCnt = 0

        override fun iteratorFor(iss: IonSchemaSystem, id: String): CloseableIterator<IonValue> {
            invokeCnt++
            throw BadAuthorityException()
        }
    }

    private val ION = IonSystemBuilder.standard().build()

    private val iss = IonSchemaSystemBuilder.standard().build()

    @Test
    fun `loadSchema(id) should throw an exception when no schema is found for that id`() {
        assertThrows<IonSchemaException> {
            iss.loadSchema("")
        }
    }

    @Test
    fun `loadSchema(id) should wrap exception when authorities throw exception and no schema is found`() {
        assertThrows<IonSchemaException> {
            IonSchemaSystemBuilder.standard()
                .addAuthority(exceptionalAuthority) // always throws BadAuthorityException
                .build()
                .loadSchema("no such schema id")
        }
    }

    @Test
    fun loadSchema_despite_authority_issues() {
        val schema = IonSchemaSystemBuilder.standard()
            .addAuthority(AuthorityFilesystem("src")) // misconfigured
            .addAuthority(exceptionalAuthority) // always throws
            .addAuthority(IonSchemaTests.authorityFor(v1_0)) // correctly configured
            .build()
            .loadSchema("schema/Customer.isl")
        assertEquals(2, exceptionalAuthority.invokeCnt) // for "Customer.isl" and "positive_int.isl"
        assertNotNull(schema)
    }

    @Test
    fun withAuthority_replaces_existing_authorities() {
        // exceptionalAuthority.iteratorFor() should not be invoked by:
        val schema = IonSchemaSystemBuilder.standard()
            .addAuthority(exceptionalAuthority)
            .withAuthority(IonSchemaTests.authorityFor(v1_0))
            .build()
            .loadSchema("schema/Customer.isl")
        assertEquals(0, exceptionalAuthority.invokeCnt)
        assertNotNull(schema)
    }

    @Test
    fun withAuthorities_replaces_existing_authorities() {
        // exceptionalAuthority.iteratorFor() should not be invoked by:
        val schema = IonSchemaSystemBuilder.standard()
            .addAuthority(exceptionalAuthority)
            .withAuthorities(listOf<Authority>(IonSchemaTests.authorityFor(v1_0)))
            .build()
            .loadSchema("schema/Customer.isl")
        assertEquals(0, exceptionalAuthority.invokeCnt)
        assertNotNull(schema)
    }

    @Test
    fun loadSchema_verifyCache() {
        val iss = IonSchemaSystemBuilder.standard()
            .addAuthority(IonSchemaTests.authorityFor(v1_0))
            .build()
        val schema = iss.loadSchema("schema/Customer.isl")
        val schemaFromCache = iss.loadSchema("schema/Customer.isl")
        assertTrue(schema == schemaFromCache)
    }

    @Test
    fun `doesSchemaDeclareType() returns a response without caching a Schema instance`() {
        val schemaCacheMock = mockk<SchemaCache>()
        val iss = IonSchemaSystemBuilder.standard()
            .withSchemaCache(schemaCacheMock)
            .addAuthority(IonSchemaTests.authorityFor(v1_0))
            .build()
        iss as IonSchemaSystemImpl

        assertTrue(iss.doesSchemaDeclareType("schema/Customer.isl", ION.newSymbol("Customer")))
        assertFalse(iss.doesSchemaDeclareType("schema/Customer.isl", ION.newSymbol("Hippopotamus")))

        // There should be no interaction with schemaCacheMock
        confirmVerified(schemaCacheMock)
    }

    @Test
    fun `listDeclaredTypes() returns a response without caching a Schema instance`() {
        val schemaCacheMock = mockk<SchemaCache>()
        val iss = IonSchemaSystemBuilder.standard()
            .withSchemaCache(schemaCacheMock)
            .addAuthority(IonSchemaTests.authorityFor(v1_0))
            .build()
        iss as IonSchemaSystemImpl

        assertEquals(
            listOf(ION.newSymbol("Customer")),
            iss.listDeclaredTypes("schema/Customer.isl")
        )

        // There should be no interaction with schemaCacheMock
        confirmVerified(schemaCacheMock)
    }

    @Test
    fun `doesSchemaDocumentExist() returns a response without caching a Schema instance`() {
        val schemaCacheMock = mockk<SchemaCache>()
        val iss = IonSchemaSystemBuilder.standard()
            .withSchemaCache(schemaCacheMock)
            .addAuthority(IonSchemaTests.authorityFor(v1_0))
            .build()
        iss as IonSchemaSystemImpl

        assertTrue(iss.doesSchemaDocumentExist("schema/Customer.isl"))
        assertFalse(iss.doesSchemaDocumentExist("schema/Hippopotamus.isl"))

        // There should be no interaction with schemaCacheMock
        confirmVerified(schemaCacheMock)
    }

    @Test
    fun newSchema() {
        val schema = iss.newSchema()
        assertFalse(schema.getTypes().hasNext())
    }

    @Test
    fun newSchema_string() {
        val schema = iss.newSchema(
            """
                type::{ name: a }
                type::{ name: b }
                type::{ name: c }
                """
        )
        assertEquals(
            listOf("a", "b", "c"),
            schema.getTypes().asSequence().toList().map { it.name }
        )
    }

    @Test
    fun newSchema_iterator() {
        val schema = iss.newSchema(
            ION.iterate(
                """
                    type::{ name: d }
                    type::{ name: e }
                    type::{ name: f }
                    """
            )
        )
        assertEquals(
            listOf("d", "e", "f"),
            schema.getTypes().asSequence().toList().map { it.name }
        )
    }

    @Test
    fun newSchema_import_unknown_schema_id() {
        assertThrows<IonSchemaException> {
            iss.newSchema(
                """
                schema_header::{
                  imports: [
                    { id: "unknown_schema_id" },
                  ],
                }
                schema_footer::{}
                """
            )
        }
    }

    @Test
    fun newSchema_unknown_type() {
        assertThrows<InvalidSchemaException> {
            iss.newSchema("type::{ type: unknown_type }")
        }
    }
}
