package software.amazon.ionschema

import org.junit.Assert.*
import org.junit.Test

class SchemaTest {
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
}

