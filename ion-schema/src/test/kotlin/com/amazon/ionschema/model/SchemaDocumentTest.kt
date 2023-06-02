package com.amazon.ionschema.model

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaVersion
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
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, listOf())
        assertNull(schema.header)
    }

    @Test
    fun `test get header when header present`() {
        val header = SchemaHeader()
        val footer = SchemaFooter()
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, listOf(header, footer))
        assertSame(header, schema.header)
    }

    @Test
    fun `test get footer when no footer present`() {
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, listOf())
        assertNull(schema.footer)
    }

    @Test
    fun `test get footer when footer present`() {
        val header = SchemaHeader()
        val footer = SchemaFooter()
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, listOf(header, footer))
        assertSame(footer, schema.footer)
    }

    @Test
    fun `test get declaredTypes when no types present`() {
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, listOf())
        assertTrue(schema.declaredTypes.isEmpty())
    }

    @Test
    fun `test get declaredTypes when some types present`() {
        val type0 = NamedTypeDefinition(
            "type0",
            TypeDefinition(
                constraints = setOf(),
            )
        )
        val type1 = NamedTypeDefinition(
            "type1",
            TypeDefinition(
                constraints = setOf(),
            )
        )
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, listOf(type0, type1))
        assertEquals(2, schema.declaredTypes.size)
        assertSame(type0, schema.declaredTypes["type0"])
        assertSame(type1, schema.declaredTypes["type1"])
    }

    @Test
    fun `test get ion schema version`() {
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, listOf())
        assertEquals(IonSchemaVersion.v2_0, schema.ionSchemaVersion)
    }

    @Test
    fun `it should be possible to retrieve open content in the original order`() {
        val type0 = NamedTypeDefinition(
            "type0",
            TypeDefinition(
                constraints = setOf(),
            )
        )
        val type1 = NamedTypeDefinition(
            "type1",
            TypeDefinition(
                constraints = setOf(),
            )
        )
        val schemaItems = listOf(
            SchemaDocument.OpenContent(ION.newInt(1)),
            SchemaDocument.OpenContent(ION.newInt(2)),
            SchemaHeader(),
            SchemaDocument.OpenContent(ION.newInt(3)),
            type0,
            SchemaDocument.OpenContent(ION.newInt(4)),
            type1,
            SchemaDocument.OpenContent(ION.newInt(5)),
            SchemaFooter(),
            SchemaDocument.OpenContent(ION.newInt(6)),
        )
        val schema = SchemaDocument("schema.isl", IonSchemaVersion.v2_0, schemaItems)
        assertIterableEquals(schemaItems, schema.items)
    }
}
