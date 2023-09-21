package com.amazon.ionschema.internal

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaSystemBuilder
import com.amazon.ionschema.Type
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TypeNullableTest {

    private val ion = IonSystemBuilder.standard().build()
    private val iss = IonSchemaSystemBuilder.standard().withIonSystem(ion).build()

    @Test
    fun `reproduce #284`() {
        // https://github.com/amazon-ion/ion-schema-kotlin/issues/284
        val schema = iss.newSchema(
            """
            type::{
              name: bat,
              any_of: [
                nullable::{valid_values: ["a"]}
              ]
            }
            """.trimIndent()
        )

        // Just ensure that we don't end up with a StackOverflowError or other exception
        schema.getType("bat")!!.let {
            it.validate(ion.singleValue("null"))
            it.validate(ion.singleValue("null.string"))
        }
    }

    @Test
    fun `nullable any should accept all values`() {
        val schema = """type::{name:foo, type:nullable::any}"""
        val type = iss.newSchema(schema).getType("foo")!!

        with(type) {
            assertValid("foo_symbol")
            assertValid("\"foo_string\"")
            assertValid("null.string")
            assertValid("null.symbol")
            assertValid("null.null")
            assertValid("null.int")
            assertValid("123")
        }
    }

    @Test
    fun `nullable nothing should accept only null typed null`() {
        val schema = """type::{name:foo, type:nullable::nothing}"""
        val type = iss.newSchema(schema).getType("foo")!!

        with(type) {
            assertValid("null.null")
            assertInvalid("null.string")
            assertInvalid("null.int")
            assertInvalid("true")
        }
    }

    @Test
    fun `nullable text should accept strings, symbols, and null`() {
        val schema = """type::{name:foo, type:nullable::text}"""
        val type = iss.newSchema(schema).getType("foo")!!

        with(type) {
            assertValid("foo_symbol")
            assertValid("\"foo_string\"")
            assertValid("null.string")
            assertValid("null.symbol")
            assertValid("null.null")
            assertInvalid("null.int")
            assertInvalid("123")
        }
    }

    private fun Type.assertValid(value: String) = assertTrue(isValid(ion.singleValue(value)))
    private fun Type.assertInvalid(value: String) = assertFalse(isValid(ion.singleValue(value)))
}
