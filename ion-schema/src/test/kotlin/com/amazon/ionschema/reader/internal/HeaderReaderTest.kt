package com.amazon.ionschema.reader.internal

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.HeaderImport
import com.amazon.ionschema.model.SchemaDocument
import com.amazon.ionschema.model.UserReservedFields
import com.amazon.ionschema.util.bagOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalIonSchemaModel::class)
class HeaderReaderTest {
    val ION = IonSystemBuilder.standard().build()
    private val headerReader = HeaderReader(IonSchemaVersion.v2_0)

    @Test
    fun `readHeader can read an empty header`() {
        val context = ReaderContext()
        val header = headerReader.readHeader(context, ION.singleValue("schema_header::{}"))
        assertEquals(SchemaDocument.Item.Header(), header)
    }

    @Test
    fun `readHeader can read a wildcard import`() {
        val context = ReaderContext()
        val header = headerReader.readHeader(context, ION.singleValue("""schema_header::{ imports: [ { id: "foo.isl" } ] }"""))
        val expected = SchemaDocument.Item.Header(imports = listOf(HeaderImport.Wildcard("foo.isl")))
        assertEquals(expected, header)
    }

    @Test
    fun `readHeader can read a type import`() {
        val context = ReaderContext()
        val header = headerReader.readHeader(context, ION.singleValue("""schema_header::{ imports: [ { id: "foo.isl", type: bar } ] }"""))
        val expected = SchemaDocument.Item.Header(imports = listOf(HeaderImport.Type("foo.isl", "bar")))
        assertEquals(expected, header)
    }

    @Test
    fun `readHeader can read an aliased import`() {
        val context = ReaderContext()
        val header = headerReader.readHeader(context, ION.singleValue("""schema_header::{ imports: [ { id: "foo.isl", type: bar, as: baz } ] }"""))
        val expected = SchemaDocument.Item.Header(imports = listOf(HeaderImport.Type("foo.isl", "bar", "baz")))
        assertEquals(expected, header)
    }

    @Test
    fun `readHeader can read user reserved fields for schema header`() {
        val context = ReaderContext()
        val header = headerReader.readHeader(context, ION.singleValue("""schema_header::{ user_reserved_fields: { schema_header: [foo] } }"""))
        val expectedUserReservedFields = UserReservedFields(header = setOf("foo"))
        val expectedHeader = SchemaDocument.Item.Header(userReservedFields = expectedUserReservedFields)
        assertEquals(expectedHeader, header)
        assertEquals(expectedUserReservedFields, context.userReservedFields)
    }

    @Test
    fun `readHeader can read user reserved fields for schema footer`() {
        val context = ReaderContext()
        val header = headerReader.readHeader(context, ION.singleValue("""schema_header::{ user_reserved_fields: { schema_footer: [foo] } }"""))
        val expectedUserReservedFields = UserReservedFields(footer = setOf("foo"))
        val expectedHeader = SchemaDocument.Item.Header(userReservedFields = expectedUserReservedFields)
        assertEquals(expectedHeader, header)
        assertEquals(expectedUserReservedFields, context.userReservedFields)
    }

    @Test
    fun `readHeader can read user reserved fields for type`() {
        val context = ReaderContext()
        val header = headerReader.readHeader(context, ION.singleValue("""schema_header::{ user_reserved_fields: { type: [foo] } }"""))
        val expectedUserReservedFields = UserReservedFields(type = setOf("foo"))
        val expectedHeader = SchemaDocument.Item.Header(userReservedFields = expectedUserReservedFields)
        assertEquals(expectedHeader, header)
        assertEquals(expectedUserReservedFields, context.userReservedFields)
    }

    @Test
    fun `readHeader can read a header with unreserved open content`() {
        val context = ReaderContext()
        val header = headerReader.readHeader(context, ION.singleValue("schema_header::{_foo:1,_bar:2}"))

        val expected = SchemaDocument.Item.Header(
            openContent = bagOf(
                "_foo" to ION.newInt(1),
                "_bar" to ION.newInt(2),
            )
        )
        assertEquals(expected, header)
    }

    @Test
    fun `readHeader can read a header with a user reserved field as open content`() {
        val context = ReaderContext()
        context.userReservedFields = UserReservedFields(footer = setOf("foo", "bar"))
        val header = headerReader.readHeader(context, ION.singleValue("schema_header::{foo:1,bar:2,user_reserved_fields:{schema_header:[foo,bar]}}"))
        val expectedUserReservedFields = UserReservedFields(header = setOf("foo", "bar"))
        val expectedHeader = SchemaDocument.Item.Header(
            userReservedFields = expectedUserReservedFields,
            openContent = bagOf(
                "foo" to ION.newInt(1),
                "bar" to ION.newInt(2),
            )
        )
        assertEquals(expectedHeader, header)
        assertEquals(expectedUserReservedFields, context.userReservedFields)
    }

    @Test
    fun `readHeader throws exception when illegal field is open content`() {
        val context = ReaderContext()
        assertThrows<InvalidSchemaException> { headerReader.readHeader(context, ION.singleValue("schema_header::{type:1}")) }
    }

    @Test
    fun `readHeader can read open content for ISL 1,0`() {
        val reader = HeaderReader(IonSchemaVersion.v1_0)
        val context = ReaderContext()
        val header = reader.readHeader(context, ION.singleValue("schema_header::{type:1,foo:2}"))

        val expected = SchemaDocument.Item.Header(
            openContent = bagOf(
                "type" to ION.newInt(1),
                "foo" to ION.newInt(2),
            )
        )
        assertEquals(expected, header)
    }

    @Test
    fun `readHeader treats user_reserved_fields as open content for ISL 1,0`() {
        val reader = HeaderReader(IonSchemaVersion.v1_0)
        val context = ReaderContext()
        val header = reader.readHeader(context, ION.singleValue("schema_header::{user_reserved_fields:{ type: [a, b, c] }}"))

        val expected = SchemaDocument.Item.Header(
            openContent = bagOf(
                "user_reserved_fields" to ION.singleValue("{type: [a, b, c]}"),
            )
        )
        assertEquals(expected, header)
        // Assert that no user reserved fields are set in the read context
        assertEquals(UserReservedFields(), context.userReservedFields)
    }
}
