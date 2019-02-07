package software.amazon.ionschema

import org.junit.Assert.*
import org.junit.Test
import software.amazon.ion.system.IonSystemBuilder

class IonSchemaImportTest {
    private val ION = IonSystemBuilder.standard().build()
    private val iss = IonSchemaSystemBuilder.standard()
            .addAuthority(AuthorityFilesystem("data/test"))
            .build()

    @Test
    fun getImportedType() {
        assertNotNull(iss.loadSchema("schema/Customer.isl").getType("positive_int"))
    }

    @Test
    fun getTypes() {
        val types = iss
                .loadSchema("schema/Customer.isl")
                .getTypes()
                .asSequence()
                .associateBy({ it.name() })
        assertTrue(types.contains("Customer"))
        assertTrue(types.contains("positive_int"))
    }

    @Test
    fun validateType() {
        val type = iss.loadSchema("schema/Customer.isl").getType("Customer")!!
        assertTrue(type.isValid(ION.singleValue("{ firstName: \"Phil\", lastName: \"Collins\" }")))
        assertFalse(type.isValid(ION.singleValue("{ firstName: \"Phil\", middleName: \"Billy\" }")))
    }

    @Test
    fun validateAgainstImportedType() {
        val type = iss.loadSchema("schema/Customer.isl").getType("Customer")!!
        assertTrue(type.isValid(ION.singleValue("{ firstName: \"Phil\", lastName: \"Collins\", age: 68}")))
        assertFalse(type.isValid(ION.singleValue("{ firstName: \"Phil\", lastName: \"Collins\", age: -1}")))
    }

}