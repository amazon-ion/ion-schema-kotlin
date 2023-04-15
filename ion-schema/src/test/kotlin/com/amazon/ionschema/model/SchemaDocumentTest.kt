package com.amazon.ionschema.model

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.SchemaDocument.Item
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@ExperimentalIonSchemaModel
class SchemaDocumentTest {

    val ION = IonSystemBuilder.standard().build()

    @Test
    fun `test get header when no header present`() {
        val schema = SchemaDocument("schema.isl", listOf())
        assertNull(schema.header)
    }

    @Test
    fun `test get header when header present`() {
        val header = Item.Header()
        val footer = Item.Footer()
        val schema = SchemaDocument("schema.isl", listOf(header, footer))
        assertSame(header, schema.header)
    }

    @Test
    fun `test get footer when no footer present`() {
        val schema = SchemaDocument("schema.isl", listOf())
        assertNull(schema.footer)
    }

    @Test
    fun `test get footer when footer present`() {
        val header = Item.Header()
        val footer = Item.Footer()
        val schema = SchemaDocument("schema.isl", listOf(header, footer))
        assertSame(footer, schema.footer)
    }

    @Test
    fun `test get declaredTypes when no types present`() {
        val schema = SchemaDocument("schema.isl", listOf())
        assertTrue(schema.declaredTypes.isEmpty())
    }

    @Test
    fun `test get declaredTypes when some types present`() {
        val type0 = NamedTypeDefinition(
            "type0",
            TypeDefinition(
                constraints = listOf(),
            )
        )
        val type1 = NamedTypeDefinition(
            "type1",
            TypeDefinition(
                constraints = listOf(),
            )
        )
        val schema = SchemaDocument("schema.isl", listOf(Item.Type(type0), Item.Type(type1)))
        assertEquals(2, schema.declaredTypes.size)
        assertSame(type0, schema.declaredTypes[0])
        assertSame(type1, schema.declaredTypes[1])
    }

    @Test
    fun `test get ion schema version when version marker is present`() {
        val schema = SchemaDocument(
            "schema.isl",
            listOf(
                Item.OpenContent(ION.newNull()),
                Item.VersionMarker(IonSchemaVersion.v2_0)
            )
        )
        assertEquals(IonSchemaVersion.v2_0, schema.ionSchemaVersion)
    }

    @Test
    fun `test get ion schema version when header is before any version marker is present`() {
        val schema = SchemaDocument(
            "schema.isl",
            listOf(
                Item.Header(),
                Item.VersionMarker(IonSchemaVersion.v2_0),
                Item.Footer(),
            )
        )
        // Should be ISL 1.0 since header was before version marker
        assertEquals(IonSchemaVersion.v1_0, schema.ionSchemaVersion)
    }

    @Test
    fun `test get ion schema version when type is before any version marker is present`() {
        val type0 = NamedTypeDefinition(
            "type0",
            TypeDefinition(
                constraints = listOf(),
            )
        )
        val schema = SchemaDocument(
            "schema.isl",
            listOf(
                Item.Type(type0),
                Item.VersionMarker(IonSchemaVersion.v2_0),
            )
        )
        // Should be ISL 1.0 since type was before version marker
        assertEquals(IonSchemaVersion.v1_0, schema.ionSchemaVersion)
    }

    @Test
    fun `it should be possible to retrieve open content in the original order`() {
        val type0 = NamedTypeDefinition(
            "type0",
            TypeDefinition(
                constraints = listOf(),
            )
        )
        val type1 = NamedTypeDefinition(
            "type1",
            TypeDefinition(
                constraints = listOf(),
            )
        )
        val schemaItems = listOf(
            Item.OpenContent(ION.newInt(1)),
            Item.VersionMarker(IonSchemaVersion.v2_0),
            Item.OpenContent(ION.newInt(2)),
            Item.Header(),
            Item.OpenContent(ION.newInt(3)),
            Item.Type(type0),
            Item.OpenContent(ION.newInt(4)),
            Item.Type(type1),
            Item.OpenContent(ION.newInt(5)),
            Item.Footer(),
            Item.OpenContent(ION.newInt(6)),
        )
        val schema = SchemaDocument("schema.isl", schemaItems)
        assertIterableEquals(schemaItems, schema.items)
    }
}
