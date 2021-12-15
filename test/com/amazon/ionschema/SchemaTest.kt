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

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.internal.SchemaCore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemaTest {
    private val ION = IonSystemBuilder.standard().build()

    private val iss = IonSchemaSystemBuilder.standard()
        .addAuthority(AuthorityFilesystem("ion-schema-tests"))
        .build()

    @Test(expected = InvalidSchemaException::class)
    fun invalid_schema_version() {
        iss.newSchema(
            """
            ${'$'}ion_schema_2_0
            type::{ name: foo, type: int }
            """.trimIndent()
        )
    }

    @Test
    fun getType() {
        assertNotNull(iss.loadSchema("schema/Customer.isl").getType("Customer"))
    }

    @Test
    fun getType_imported() {
        assertNotNull(iss.loadSchema("schema/Customer.isl").getType("positive_int"))
    }

    @Test
    fun getType_unknown() {
        assertNull(iss.loadSchema("schema/Customer.isl").getType("unknown_type"))
    }

    @Test
    fun getTypes() {
        val types = iss
            .loadSchema("schema/Customer.isl")
            .getTypes()
            .asSequence()
            .associateBy { it.name }
        assertTrue(types.contains("Customer"))
        assertTrue(types.contains("positive_int")) // a type imported into Customer.isl
    }

    @Test
    fun getDeclaredType() {
        assertNotNull(iss.loadSchema("schema/Customer.isl").getDeclaredType("Customer"))
    }

    @Test
    fun getDeclaredType_imported() {
        assertNull(iss.loadSchema("schema/Customer.isl").getDeclaredType("positive_int"))
    }

    @Test
    fun getDeclaredType_unknown() {
        assertNull(iss.loadSchema("schema/Customer.isl").getDeclaredType("unknown_type"))
    }

    @Test
    fun getDeclaredTypes() {
        val types = iss
            .loadSchema("schema/Customer.isl")
            .getDeclaredTypes()
            .asSequence()
            .associateBy { it.name }
        assertEquals(setOf("Customer"), types.keys)
    }

    @Test
    fun getSchemaSystem() {
        assertTrue(iss === iss.loadSchema("schema/Customer.isl").getSchemaSystem())
    }

    @Test
    fun newType_string() {
        val type = iss.newSchema().newType("type::{ codepoint_length: 5 }")
        assertTrue(type.isValid(ION.singleValue("abcde")))
        assertFalse(type.isValid(ION.singleValue("abcd")))
        assertFalse(type.isValid(ION.singleValue("abcdef")))
    }

    @Test
    fun newType_struct() {
        val type = iss.newSchema().newType(
            ION.singleValue("type::{ codepoint_length: 5 }") as IonStruct
        )
        assertTrue(type.isValid(ION.singleValue("abcde")))
        assertFalse(type.isValid(ION.singleValue("abcd")))
        assertFalse(type.isValid(ION.singleValue("abcdef")))
    }

    @Test
    fun newType_uses_type_from_schema() {
        // the "positive_int" type is imported by schema/Customer.isl;
        // assert that a type created with newType() is able to use it
        val schema = iss.loadSchema("schema/Customer.isl")
        val type = schema.newType("type::{ fields: { a: positive_int } }")
        assertTrue(type.isValid(ION.singleValue("{ a: 1 }")))
        assertFalse(type.isValid(ION.singleValue("{ a: -1 }")))
    }

    @Test(expected = InvalidSchemaException::class)
    fun newType_unknown_type() {
        iss.newSchema().newType("type::{ type: unknown_type }")
    }

    @Test
    fun plusType() {
        val schema = iss.newSchema()

        val type1 = schema.newType("type::{ name: A, codepoint_length: 3 }")
        val schema1 = schema.plusType(type1)

        val type2 = schema.newType("type::{ name: A, codepoint_length: 5 }")
        val schema2 = schema1.plusType(type2)

        // verify the type remains unchanged in the original schema
        val typeA1 = schema1.getType("A")!!
        assertFalse(typeA1.isValid(ION.singleValue("ab")))
        assertTrue(typeA1.isValid(ION.singleValue("abc")))
        assertFalse(typeA1.isValid(ION.singleValue("abcd")))

        // verify the type reflects new behavior when retrieved from the newer schema instance
        val typeA2 = schema2.getType("A")!!
        assertFalse(typeA2.isValid(ION.singleValue("abcd")))
        assertTrue(typeA2.isValid(ION.singleValue("abcde")))
        assertFalse(typeA2.isValid(ION.singleValue("abcdef")))

        // verify a new type 'B' isn't available from the earlier schema instances
        val type3 = schema.newType("type::{ name: B }")
        val schema3 = schema2.plusType(type3)
        assertNull(schema1.getType("B"))
        assertNull(schema2.getType("B"))
        assertNotNull(schema3.getType("B"))

        // verify the original schema remains empty
        assertEquals(0, schema.getTypes().asSequence().count())
    }

    @Test(expected = InvalidSchemaException::class)
    fun plusType_unnamed_type() {
        val schema = iss.newSchema()
        val unnamedType = schema.newType("type::{ codepoint_length: 3 }")
        schema.plusType(unnamedType)
    }

    @Test
    fun plusType_imports_retained() {
        val schema = iss.loadSchema("schema/import/import_types.isl")
        assertEquals(2, schema.getImports().asSequence().count())
        val newType = schema.newType("type::{name: three, value_values: [3], open_content: 3}")
        val newSchema = schema.plusType(newType)
        assertEquals(
            schema.getImports().asSequence().toList(),
            newSchema.getImports().asSequence().toList()
        )
    }

    @Test
    fun param_allow_anonymous_top_level_types() {
        val iss = IonSchemaSystemBuilder.standard()
            .allowAnonymousTopLevelTypes()
            .build()
        val schema = iss.newSchema()
        val unnamedType = schema.newType("type::{ codepoint_length: 3 }")
        schema.plusType(unnamedType)
    }

    private val islTemplate =
        """
            open_content
            schema_header::{open_content: hi}
            open_content
            type::{name: one, valid_values: [1], open_content: 1}
            open_content
            type::{name: two, valid_values: [2], open_content: 2}
            open_content
            %s
            schema_footer::{open_content: bye}
            open_content
        """.trimIndent()

    @Test
    fun isl() {
        val isl = islTemplate.format("")
        val schema = iss.newSchema(isl)
        assertEquals(ION.newLoader().load(isl), schema.isl)
        assertTrue(schema.isl.isReadOnly)
        assertNull(schema.isl.container)
    }

    @Test
    fun isl_plusType() {
        val isl = islTemplate.format("")
        val schema = iss.newSchema(isl)
        val type3isl = "type::{name: three, value_values: [3], open_content: 3}"
        val newType = schema.newType(type3isl)
        val newSchema = schema.plusType(newType)

        val newIsl = islTemplate.format(type3isl)
        assertNotEquals(isl, newIsl)
        assertEquals(ION.newLoader().load(newIsl), newSchema.isl)
        assertTrue(newSchema.isl.isReadOnly)
        assertNull(newSchema.getType("three")!!.isl.container)
    }

    @Test
    fun isl_plusType_replace() {
        val isl = islTemplate.format("")
        val schema = iss.newSchema(isl)
        val replacementType2isl = "type::{name: two, valid_values: [222], open_content: 222}"
        val newType = schema.newType(replacementType2isl)
        val newSchema = schema.plusType(newType)

        val newIsl = islTemplate.format("").replace("2", "222")
        assertNotEquals(isl, newIsl)
        assertEquals(ION.newLoader().load(newIsl), newSchema.isl)
        assertTrue(newSchema.isl.isReadOnly)
        assertNull(schema.getType("two")!!.isl.container)
    }

    @Test
    fun isl_SchemaCore() {
        val schemaCore = SchemaCore(iss)
        assertEquals(ION.newDatagram(), schemaCore.isl)
        assertTrue(schemaCore.isl.isReadOnly)
        assertNull(schemaCore.isl.container)
    }
}
