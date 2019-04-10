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
    fun plusType_string() {
        plusType(arrayOf("type::{ name: A, codepoint_length: 3 }",
                         "type::{ name: A, codepoint_length: 5 }",
                         "type::{ name: B }"),
                 null)
    }

    @Test
    fun plusType_struct() {
        plusType(null,
                 arrayOf(structOf("type::{ name: A, codepoint_length: 3 }"),
                         structOf("type::{ name: A, codepoint_length: 5 }"),
                         structOf("type::{ name: B }")))
    }

    private fun structOf(s: String) = ION.singleValue(s) as IonStruct

    private fun plusType(strings: Array<String>?, structs: Array<IonStruct>?) {
        val schema = iss.newSchema()
        val schema1 = when {
            strings != null -> schema.plusType(strings[0])
            structs != null -> schema.plusType(structs[0])
            else -> throw UnsupportedOperationException()
        }
        val schema2 = when {
            strings != null -> schema1.plusType(strings[1])
            structs != null -> schema1.plusType(structs[1])
            else -> throw UnsupportedOperationException()
        }

        // verify the type remains unchanged in the original schema
        val type3 = schema1.getType("A")!!
        assertFalse(type3.isValid(ION.singleValue("ab")))
        assertTrue (type3.isValid(ION.singleValue("abc")))
        assertFalse(type3.isValid(ION.singleValue("abcd")))

        // verify the type reflects new behavior when retrieved from the newer schema instance
        val type5 = schema2.getType("A")!!
        assertFalse(type5.isValid(ION.singleValue("abcd")))
        assertTrue (type5.isValid(ION.singleValue("abcde")))
        assertFalse(type5.isValid(ION.singleValue("abcdef")))

        // verify a new type 'B' isn't available from the earlier schema instances
        val schema3 = when {
            strings != null -> schema1.plusType(strings[2])
            structs != null -> schema1.plusType(structs[2])
            else -> throw UnsupportedOperationException()
        }
        assertNull   (schema1.getType("B"))
        assertNull   (schema2.getType("B"))
        assertNotNull(schema3.getType("B"))
    }
}

