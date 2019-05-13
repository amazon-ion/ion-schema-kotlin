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
import software.amazon.ion.IonValue
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.util.CloseableIterator

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

    @Test(expected = IonSchemaException::class)
    fun unresolvableSchema() {
        iss.loadSchema("")
    }

    @Test
    fun loadSchema_despite_authority_issues() {
        val schema = IonSchemaSystemBuilder.standard()
                .addAuthority(AuthorityFilesystem("data"))       // misconfigured
                .addAuthority(exceptionalAuthority)              // always throws
                .addAuthority(AuthorityFilesystem("data/test"))  // correctly configured
                .build()
                .loadSchema("schema/Customer.isl")
        assertEquals(2, exceptionalAuthority.invokeCnt)   // for "Customer.isl" and "positive_int.isl"
        assertNotNull(schema)
    }

    @Test
    fun withAuthority_replaces_existing_authorities() {
        // exceptionalAuthority.iteratorFor() should not be invoked by:
        val schema = IonSchemaSystemBuilder.standard()
                .addAuthority(exceptionalAuthority)
                .withAuthority(AuthorityFilesystem("data/test"))
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
                .withAuthorities(listOf<Authority>(AuthorityFilesystem("data/test")))
                .build()
                .loadSchema("schema/Customer.isl")
        assertEquals(0, exceptionalAuthority.invokeCnt)
        assertNotNull(schema)
    }

    @Test
    fun newSchema() {
        val schema = iss.newSchema()
        assertFalse(schema.getTypes().hasNext())
    }

    @Test
    fun newSchema_string() {
        val schema = iss.newSchema("""
                type::{ name: a }
                type::{ name: b }
                type::{ name: c }
                """)
        assertEquals(listOf("a", "b", "c"),
                schema.getTypes().asSequence().toList().map { it.name })
    }

    @Test
    fun newSchema_iterator() {
        val schema = iss.newSchema(
                ION.iterate("""
                    type::{ name: d }
                    type::{ name: e }
                    type::{ name: f }
                    """))
        assertEquals(listOf("d", "e", "f"),
                schema.getTypes().asSequence().toList().map { it.name })
    }

    @Test(expected = IonSchemaException::class)
    fun newSchema_import_unknown_schema_id() {
        iss.newSchema("""
            schema_header::{
              imports: [
                { id: "unknown_schema_id" },
              ],
            }
            schema_footer::{}
            """)
    }

    @Test(expected = InvalidSchemaException::class)
    fun newSchema_unknown_type() {
        iss.newSchema("type::{ type: unknown_type }")
    }
}

