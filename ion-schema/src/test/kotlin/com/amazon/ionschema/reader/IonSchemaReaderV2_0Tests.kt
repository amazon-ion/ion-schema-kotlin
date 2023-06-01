package com.amazon.ionschema.reader

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.HeaderImport
import com.amazon.ionschema.model.NamedTypeDefinition
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.model.UserReservedFields
import com.amazon.ionschema.util.emptyBag
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@OptIn(ExperimentalIonSchemaModel::class)
class IonSchemaReaderV2_0Tests {

    val ION = IonSystemBuilder.standard().build()
    val reader = IonSchemaReaderV2_0()
    val typeTextWithMultipleErrors = """
            type::{
              valid_values: until_recently::["Ni!"],
              not: "it"
            }
        """

    val namedTypeTextWithMultipleErrors = """
            type::{
              name: foo,
              codepoint_length: three,
              utf8_byte_length: five,
              valid_values: [1, 2, 5, "Three, sir!", 3],
            }
        """

    val schemaTextWithMultipleErrors = """
            ${'$'}ion_schema_2_0
            $namedTypeTextWithMultipleErrors
            $typeTextWithMultipleErrors
            type::null.symbol
        """

    @Test
    fun `readSchema can read a schema with types`() {
        val ionStream = ION.loader.load(
            """
            ${'$'}ion_schema_2_0
            type::{
              name: type1,
              type: string,
              codepoint_length: 3,
            }
            type::{
              name: type2,
              type: symbol,
              utf8_byte_length: 5
            } 
        """
        )

        val result = reader.readSchema(ionStream)

        assertTrue(result.isOk())
        assertEquals(
            SchemaDocument(
                id = null,
                ionSchemaVersion = IonSchemaVersion.v2_0,
                items = listOf(
                    SchemaDocument.Item.Type(
                        NamedTypeDefinition(
                            typeName = "type1",
                            typeDefinition = TypeDefinition(
                                constraints = setOf(
                                    Constraint.Type(TypeArgument.Reference("string")),
                                    Constraint.CodepointLength(DiscreteIntRange(3))
                                )
                            ),
                        )
                    ),
                    SchemaDocument.Item.Type(
                        NamedTypeDefinition(
                            typeName = "type2",
                            typeDefinition = TypeDefinition(
                                constraints = setOf(
                                    Constraint.Type(TypeArgument.Reference("symbol")),
                                    Constraint.Utf8ByteLength(DiscreteIntRange(5))
                                )
                            ),
                        )
                    )
                )
            ),
            result.unwrap()
        )
    }

    @Test
    fun `readSchema can read a schema with a header`() {
        val ionStream = ION.loader.load(
            """
            ${'$'}ion_schema_2_0
            schema_header::{
              imports: [
                { id: "foo.isl" }
              ],
              user_reserved_fields: {
                type: [foo],  
              }
            }
        """
        )

        val result = reader.readSchema(ionStream)

        assertTrue(result.isOk())
        assertEquals(
            SchemaDocument(
                id = null,
                ionSchemaVersion = IonSchemaVersion.v2_0,
                items = listOf(
                    SchemaDocument.Item.Header(
                        imports = listOf(HeaderImport.Wildcard("foo.isl")),
                        userReservedFields = UserReservedFields(type = setOf("foo"))
                    ),
                )
            ),
            result.unwrap()
        )
    }

    @Test
    fun `readSchema can read a schema with open content`() {
        val ionStream = ION.loader.load(
            """
            foo
            // Only bar{N} is open content. Both 'foo' and 'baz' are outside the schema. 
            ${'$'}ion_schema_2_0
            bar1
            schema_header::{}
            bar2
            type::{ name: foo }
            bar3
            schema_footer::{}
            baz
        """
        )

        val result = reader.readSchema(ionStream)

        assertTrue(result.isOk())
        assertEquals(
            SchemaDocument(
                id = null,
                ionSchemaVersion = IonSchemaVersion.v2_0,
                items = listOf(
                    SchemaDocument.Item.OpenContent(ION.singleValue("bar1")),
                    SchemaDocument.Item.Header(),
                    SchemaDocument.Item.OpenContent(ION.singleValue("bar2")),
                    SchemaDocument.Item.Type(NamedTypeDefinition("foo", TypeDefinition(emptySet(), emptyBag()))),
                    SchemaDocument.Item.OpenContent(ION.singleValue("bar3")),
                    SchemaDocument.Item.Footer(),
                )
            ),
            result.unwrap()
        )
    }

    @Test
    fun `readSchema can return errors in a schema using slow-fail`() {
        val ionStream = ION.loader.load(schemaTextWithMultipleErrors)

        val result = reader.readSchema(ionStream, failFast = false)

        assertTrue(result.isErr())
        val errs = result.errValueOrNull()!!
        // The actual errors are not important. Just check that we got more than one error.
        assertTrue(errs.size > 1)
    }

    @Test
    fun `readSchema can return errors in a schema using fast-fail`() {
        val ionStream = ION.loader.load(schemaTextWithMultipleErrors)

        val result = reader.readSchema(ionStream, failFast = true)

        assertTrue(result.isErr())
        val errs = result.errValueOrNull()!!
        // The actual errors are not important. Just check that we got exactly one error.
        assertTrue(errs.size == 1)
    }

    @Test
    fun `readSchemaOrThrow throws an InvalidSchemaException for a bad schema`() {
        val ionStream = ION.loader.load(schemaTextWithMultipleErrors)

        assertThrows<InvalidSchemaException> { reader.readSchemaOrThrow(ionStream) }
    }

    @Test
    fun `readType can read a type`() {
        val ionStream = ION.singleValue("type::{ type: string, codepoint_length: 5 }")

        val result = reader.readType(ionStream)

        assertTrue(result.isOk())
        assertEquals(
            TypeDefinition(
                constraints = setOf(
                    Constraint.Type(TypeArgument.Reference("string")),
                    Constraint.CodepointLength(DiscreteIntRange(5))
                )
            ),
            result.unwrap()
        )
    }

    @Test
    fun `readType can return errors in a type using slow-fail`() {
        val ionStream = ION.singleValue(typeTextWithMultipleErrors)

        val result = reader.readType(ionStream, failFast = false)

        assertTrue(result.isErr())
        val errs = result.errValueOrNull()!!
        // The actual errors are not important. Just check that we got more than one error.
        assertTrue(errs.size > 1)
    }

    @Test
    fun `readType can return errors in a type using fast-fail`() {
        val ionStream = ION.singleValue(typeTextWithMultipleErrors)

        val result = reader.readType(ionStream, failFast = true)

        assertTrue(result.isErr())
        val errs = result.errValueOrNull()!!
        // The actual errors are not important. Just check that we got exactly one error.
        assertTrue(errs.size == 1)
    }

    @Test
    fun `readTypeOrThrow throws an InvalidSchemaException for a bad type`() {
        val ionStream = ION.singleValue(typeTextWithMultipleErrors)

        assertThrows<InvalidSchemaException> { reader.readTypeOrThrow(ionStream) }
    }

    @Test
    fun `readNamedType can read a type`() {
        val ionStream = ION.singleValue("type::{ name: foo, type: string }")

        val result = reader.readNamedType(ionStream)

        assertTrue(result.isOk())
        assertEquals(
            NamedTypeDefinition(
                typeName = "foo",
                typeDefinition = TypeDefinition(
                    constraints = setOf(Constraint.Type(TypeArgument.Reference("string")))
                )
            ),
            result.unwrap()
        )
    }

    @Test
    fun `readNamedType can return errors in a type using slow-fail`() {
        val ionStream = ION.singleValue(namedTypeTextWithMultipleErrors)

        val result = reader.readNamedType(ionStream, failFast = false)

        assertTrue(result.isErr())
        val errs = result.errValueOrNull()!!
        // The actual errors are not important. Just check that we got more than one error.
        assertTrue(errs.size > 1)
    }

    @Test
    fun `readNamedType can return errors in a type using fast-fail`() {
        val ionStream = ION.singleValue(namedTypeTextWithMultipleErrors)

        val result = reader.readNamedType(ionStream, failFast = true)

        assertTrue(result.isErr())
        val errs = result.errValueOrNull()!!
        // The actual errors are not important. Just check that we got exactly one error.
        assertTrue(errs.size == 1)
    }

    @Test
    fun `readNamedTypeOrThrow throws an InvalidSchemaException for a bad type`() {
        val ionStream = ION.singleValue(namedTypeTextWithMultipleErrors)

        assertThrows<InvalidSchemaException> { reader.readNamedTypeOrThrow(ionStream) }
    }
}
