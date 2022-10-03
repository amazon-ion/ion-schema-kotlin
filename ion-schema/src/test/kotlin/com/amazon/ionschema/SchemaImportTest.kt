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

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaVersion.v1_0
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SchemaImportTest {
    private val ION = IonSystemBuilder.standard().build()

    private val iss = IonSchemaSystemBuilder.standard()
        .addAuthority(IonSchemaTests.authorityFor(v1_0))
        .build()

    @Test
    fun getImport_unknown() {
        val schema = iss.loadSchema("schema/import/import.isl")
        assertNull(schema.getImport("unknown_id"))
    }

    @Test
    fun getImport_entire_schema() {
        val schema = iss.loadSchema("schema/import/import.isl")
        val schemaId = "schema/import/abcde.isl"
        val import = schema.getImport(schemaId)!!
        assertEquals(schemaId, import.id)
        assertEquals(5, import.getTypes().asSequence().count())
        assertEquals(5, import.getSchema().getTypes().asSequence().count())
        import.getTypes().forEach {
            val type = import.getType(it.name)
            assertNotNull(type)
        }
    }

    @Test
    fun getImport_id_is_a_symbol() {
        val test = "schema_header::{ imports: [ {id: 'schema/import/abcde.isl' }] } schema_footer::{}"
        val ion = IonSystemBuilder.standard().build()
        val iss = IonSchemaSystemBuilder.standard()
            .addAuthority(IonSchemaTests.authorityFor(v1_0)).withIonSystem(ion).build()
        val schema = iss.newSchema(ion.iterate(test))
        val schemaId = "schema/import/abcde.isl"
        val import = schema.getImport(schemaId)!!
        assertEquals(schemaId, import.id)
        assertEquals(5, import.getTypes().asSequence().count())
        assertNotNull(import.getSchema())
    }

    @Test
    fun getImport_type() {
        val schema = iss.loadSchema("schema/import/import_type.isl")
        val schemaId = "schema/util/positive_int.isl"
        val import = schema.getImport(schemaId)!!
        assertEquals(schemaId, import.id)
        assertEquals(1, import.getTypes().asSequence().count())
        val type = import.getType("positive_int")!!
        assertEquals("positive_int", type.name)
        assertNotNull(import.getSchema())
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
        assertEquals(2, import.getTypes().asSequence().count())
        assertNotNull(import.getType("positive_int_1"))
        assertNotNull(import.getType("positive_int_2"))
        assertNotNull(import.getSchema().getType("positive_int"))
    }

    @Test
    fun getImports_multiple_aliased_types() {
        val schema = iss.loadSchema("schema/import/import_types.isl")
        val keys = mapOf(
            "schema/import/abcde.isl" to setOf("a2", "b", "c2"),
            "schema/util/positive_int.isl" to setOf("positive_int", "posint")
        )

        assertEquals(keys.size, schema.getImports().asSequence().count())
        keys.entries.forEach { entry ->
            val import = schema.getImport(entry.key)!!
            assertEquals(entry.key, import.id)
            assertEquals(entry.value.size, import.getTypes().asSequence().count())
            entry.value.forEach {
                val type = import.getType(it)!!
                assertEquals(it, type.name)
            }
        }

        assertEquals(
            1,
            schema.getImport("schema/util/positive_int.isl")!!.getSchema()
                .getTypes().asSequence().count()
        )
        assertEquals(
            5,
            schema.getImport("schema/import/abcde.isl")!!.getSchema()
                .getTypes().asSequence().count()
        )
    }
}
