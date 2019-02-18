package software.amazon.ionschema

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IonSchemaImportTest {
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

}