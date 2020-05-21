/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import org.junit.Assert.*
import org.junit.Test
import com.amazon.ion.system.IonSystemBuilder

class SchemaImportTest {
    private val ION = IonSystemBuilder.standard().build()

    private val iss = IonSchemaSystemBuilder.standard()
            .addAuthority(AuthorityFilesystem("ion-schema-tests"))
            .build()

    @Test
    fun getImport_unknown() {
        val schema = iss.loadSchema("schema/import/import.isl")
        assertNull(schema.getImport("unknown_id"))
    }

    @Test
    fun getImport_entire_schema() {
        val schema = iss.loadSchema("schema/import/import.isl")
        val id = "schema/import/abcde.isl"
        val import = schema.getImport(id)!!
        assertEquals(id, import.id)

        assertEquals(5, import.getTypes().asSequence().count())
        import.getTypes().forEach {
            val type = import.getType(it.name)
            assertNotNull(type)
        }
    }

    @Test
    fun getImport_type() {
        val schema = iss.loadSchema("schema/import/import_type.isl")
        val id = "schema/util/positive_int.isl"
        val import = schema.getImport(id)!!
        assertEquals(id, import.id)

        assertEquals(1, import.getTypes().asSequence().count())
        val type = import.getType("positive_int")!!
        assertEquals("positive_int", type.name)
    }

    @Test
    fun getImport_multiple_aliased_types() {
        val schema = iss.loadSchema("schema/import/import_types.isl")
        val keys = mapOf(
                "schema/import/abcde.isl"      to setOf("a2", "b", "c2"),
                "schema/util/positive_int.isl" to setOf("positive_int", "posint"))

        assertEquals(keys.size, schema.getImports().asSequence().count())
        keys.entries.forEach { entry ->
            val import = schema.getImport(entry.key)!!
            assertEquals(entry.value.size, import.getTypes().asSequence().count())
            entry.value.forEach {
                val type = import.getType(it)!!
                assertEquals(it, type.name)
            }
        }
    }

    @Test
    fun getImports_none() {
        val schema = iss.loadSchema("schema/byte_length.isl")
        assertEquals(0, schema.getImports().asSequence().count())
    }

    @Test
    fun getImports() {
        val schema = iss.loadSchema("schema/import/import_type_by_alias.isl")
        assertEquals(1, schema.getImports().asSequence().count())
        val import = schema.getImports().next()
        assertEquals("schema/util/positive_int.isl", import.id)
    }
}

