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
    fun getType_unknownType() {
        assertNull(iss.loadSchema("schema/Customer.isl").getType("unknownType"))
    }

    @Test
    fun getTypes() {
        val types = iss
                .loadSchema("schema/Customer.isl")
                .getTypes()
                .asSequence()
                .associateBy({ it.name() })
        assertTrue(types.contains("Customer"))
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
}

