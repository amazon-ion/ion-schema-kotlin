package software.amazon.ionschema

import org.junit.Assert.*
import org.junit.Test
import software.amazon.ion.IonStruct
import software.amazon.ion.system.IonSystemBuilder

class SchemaTest {
    private val ION = IonSystemBuilder.standard().build()

    private val iss = IonSchemaSystemBuilder.standard()
            .addAuthority(AuthorityFilesystem("data/test"))
            .build()

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
        assertTrue(types.contains("positive_int"))   // a type imported into Customer.isl
    }

    @Test
    fun getSchemaSystem() {
        assertTrue(iss === iss.loadSchema("schema/Customer.isl").getSchemaSystem())
    }

    @Test
    fun newType_string() {
        val type = iss.newSchema().newType("type::{ codepoint_length: 5 }")
        assertTrue (type.isValid(ION.singleValue("abcde")))
        assertFalse(type.isValid(ION.singleValue("abcd")))
        assertFalse(type.isValid(ION.singleValue("abcdef")))
    }

    @Test
    fun newType_struct() {
        val type = iss.newSchema().newType(
                ION.singleValue("type::{ codepoint_length: 5 }") as IonStruct)
        assertTrue (type.isValid(ION.singleValue("abcde")))
        assertFalse(type.isValid(ION.singleValue("abcd")))
        assertFalse(type.isValid(ION.singleValue("abcdef")))
    }

    @Test
    fun newType_uses_type_from_schema() {
        // the "positive_int" type is imported by schema/Customer.isl;
        // assert that a type created with newType() is able to use it
        val schema = iss.loadSchema("schema/Customer.isl")
        val type = schema.newType("type::{ fields: { a: positive_int } }")
        assertTrue (type.isValid(ION.singleValue("{ a: 1 }")))
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
        assertTrue (typeA1.isValid(ION.singleValue("abc")))
        assertFalse(typeA1.isValid(ION.singleValue("abcd")))

        // verify the type reflects new behavior when retrieved from the newer schema instance
        val typeA2 = schema2.getType("A")!!
        assertFalse(typeA2.isValid(ION.singleValue("abcd")))
        assertTrue (typeA2.isValid(ION.singleValue("abcde")))
        assertFalse(typeA2.isValid(ION.singleValue("abcdef")))

        // verify a new type 'B' isn't available from the earlier schema instances
        val type3 = schema.newType("type::{ name: B }")
        val schema3 = schema2.plusType(type3)
        assertNull   (schema1.getType("B"))
        assertNull   (schema2.getType("B"))
        assertNotNull(schema3.getType("B"))

        // verify the original schema remains empty
        assertEquals(0, schema.getTypes().asSequence().count())
    }
}

