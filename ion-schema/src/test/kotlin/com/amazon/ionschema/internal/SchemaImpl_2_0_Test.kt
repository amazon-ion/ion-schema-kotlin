package com.amazon.ionschema.internal

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaSystemBuilder
import com.amazon.ionschema.IonSchemaTests
import com.amazon.ionschema.IonSchemaVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SchemaImpl_2_0_Test {

    val ISS = IonSchemaSystemBuilder.standard()
        .allowTransitiveImports(false)
        .addAuthority(IonSchemaTests.authorityFor(IonSchemaVersion.v2_0))
        .build()

    fun ionStream(vararg topLevelValues: String): List<IonValue> {
        return topLevelValues.map { ISS.ionSystem.singleValue(it) }
    }

    fun ionStruct(ion: String) = ISS.ionSystem.singleValue(ion) as IonStruct

    @Test
    fun `newType(isl) should create a new Type instance without modifying the schema`() {
        val schema = ISS.newSchema("\$ion_schema_2_0 type::{ name: foo, type: int }")
        val newType = schema.newType("type::{ name: bar }")

        val oldSchemaTypes = schema.getDeclaredTypes().asSequence().map { it.name }.toSet()
        val expectedOldSchemaTypes = setOf("foo")
        assertEquals(expectedOldSchemaTypes, oldSchemaTypes, "The original schema should not be modified.")
    }

    @Test
    fun `plusType(type) should return a new schema instance that contains the new type and the types from the original schema`() {
        val schema = ISS.newSchema("\$ion_schema_2_0 type::{ name: foo }")
        val newType = schema.newType("type::{ name: bar }")
        val newSchema = schema.plusType(newType)

        val oldSchemaTypes = schema.getDeclaredTypes().asSequence().map { it.name }.toSet()
        val expectedOldSchemaTypes = setOf("foo")
        assertEquals(expectedOldSchemaTypes, oldSchemaTypes, "The original schema should not be modified.")

        val newSchemaTypes = newSchema.getDeclaredTypes().asSequence().map { it.name }.toSet()
        val expectedNewSchemaTypes = setOf("foo", "bar")
        assertEquals(expectedNewSchemaTypes, newSchemaTypes)
    }

    @Test
    fun `plusType(type) should return a new schema instance, replacing an existing type with the same name`() {
        val schema = ISS.newSchema("\$ion_schema_2_0 type::{ name: foo }")
        val newType = schema.newType("type::{ name: foo, type: string }")
        val newSchema = schema.plusType(newType)

        val oldFooTypeIsl = schema.getType("foo")?.isl
        val expectedOldFooTypeIsl = ionStruct("type::{ name: foo }")
        assertEquals(expectedOldFooTypeIsl, oldFooTypeIsl, "The 'foo' type in the original schema should not be modified.")

        val newFooTypeIsl = newSchema.getType("foo")?.isl
        val expectedNewFooTypeIsl = ionStruct("type::{ name: foo, type: string }")
        assertEquals(expectedNewFooTypeIsl, newFooTypeIsl)
    }

    @Test
    fun `getType(name) should return a declared type`() {
        val schema = ISS.newSchema("\$ion_schema_2_0 type::{ name: foo }")
        assertNotNull(schema.getType("foo"))
    }

    @Test
    fun `getType(name) should return a built-in type`() {
        val schema = ISS.newSchema("\$ion_schema_2_0 type::{ name: foo }")
        assertNotNull(schema.getType("text"))
    }

    @Test
    fun `getType(name) should not return an imported type`() {
        val schema = ISS.newSchema(
            ionStream(
                "\$ion_schema_2_0",
                """ schema_header::{ imports: [ { id: "util.isl" } ] } """,
                "type::{ name: foo, type: positive_int }",
                "schema_footer::{}",
            ).iterator()
        )
        val type = schema.getType("positive_int")
        assertNull(type)
    }

    @Test
    fun `getDeclaredType() should return a declared type`() {
        val schema = ISS.newSchema("\$ion_schema_2_0 type::{ name: foo }")
        val type = schema.getDeclaredType("foo")
        assertNotNull(type)
    }

    @Test
    fun `getDeclaredType() should not return a built-in type`() {
        val schema = ISS.newSchema("\$ion_schema_2_0 type::{ name: foo }")
        val type = schema.getDeclaredType("text")
        assertNull(type)
    }

    @Test
    fun `getDeclaredType() should not return an imported type`() {
        val schema = ISS.newSchema(
            ionStream(
                "\$ion_schema_2_0",
                """ schema_header::{ imports: [ { id: "util.isl" } ] } """,
                "type::{ name: foo, type: positive_int }",
                "schema_footer::{}",
            ).iterator()
        )
        val type = schema.getDeclaredType("positive_int")
        assertNull(type)
    }

    @Test
    fun `getDeclaredTypes() should return only top level types that are declared in the schema`() {
        val schema = ISS.newSchema(
            ionStream(
                "\$ion_schema_2_0",
                """ schema_header::{ imports: [ { id: "util.isl" } ] } """,
                "type::{ name: foo }",
                "type::{ name: bar }",
                "type::{ name: baz }",
                "schema_footer::{}",
            ).iterator()
        )

        val result = schema.getDeclaredTypes().asSequence().map { it.name }.toSet()
        val expected = setOf("foo", "bar", "baz")

        assertEquals(expected, result)
    }

    @Test
    fun `getTypes() should return built-in types and declared types`() {
        val schema = ISS.newSchema(
            ionStream(
                "\$ion_schema_2_0",
                """ schema_header::{ imports: [ { id: "util.isl" } ] } """,
                "type::{ name: foo, type: positive_int }",
                "type::{ name: bar }",
                "type::{ name: baz }",
                "schema_footer::{}",
            ).iterator()
        ) as SchemaImpl_2_0

        val result = schema.getTypes().asSequence().map { it.name }.toSet()

        assertTrue(result.containsAll(setOf("foo", "bar", "baz")), "Result should contain the declared types")
        assertFalse(result.contains("positive_int"), "Result should not contain the imported types")
        assertTrue(result.contains("text"), "Result should contain built-in types")
    }

    @Test
    fun `getImports() should return the imports objects`() {
        val schema = ISS.newSchema(
            ionStream(
                "\$ion_schema_2_0",
                """ schema_header::{ imports: [ { id: "util.isl" } ] } """,
                "type::{ name: foo, type: positive_int }",
                "type::{ name: bar }",
                "type::{ name: baz }",
                "schema_footer::{}",
            ).iterator()
        ) as SchemaImpl_2_0
        // Unfortunately, [ImportImpl] doesn't have a proper definition of equality, so we're converting to a
        // Map<String, List<String>>> which is like a multimap of schemaId to type names imported from that schema.
        assertEquals(
            mapOf("util.isl" to setOf("positive_int")),
            schema.getImports().asSequence().associate { it.id to it.getTypes().asSequence().map { it.name }.toSet() },
        )
    }

    @Test
    fun `getImport(id) should return the imports for the given schema`() {
        val schema = ISS.newSchema(
            ionStream(
                "\$ion_schema_2_0",
                """ schema_header::{ imports: [ { id: "util.isl" } ] } """,
                "type::{ name: foo, type: positive_int }",
                "type::{ name: bar }",
                "type::{ name: baz }",
                "schema_footer::{}",
            ).iterator()
        ) as SchemaImpl_2_0

        val import = schema.getImport("util.isl")
        assertNotNull(import)
        // Unfortunately, [ImportImpl] doesn't have a proper definition of equality, so we're just comparing the type
        // names in the import
        assertEquals(
            setOf("positive_int"),
            import?.getTypes()?.asSequence()?.map { it.name }?.toSet(),
        )
    }
}
