package software.amazon.ionschema

import org.junit.Test

class IonSchemaSystemTest {
    private val schemaSystem = IonSchemaSystemBuilder
            .standard()
            .addAuthority(AuthorityFilesystem("."))
            .build()

    @Test
    fun testUnknownType() {
        // TBD
        //schemaSystem.loadSchema("").getType("unknown_type")  // expect exception
    }

    @Test
    fun authorityTest() {
        val schema = schemaSystem.loadSchema("data/test/schema/Customer.isl")
        schema.getTypes().forEach { println(it.name()) }
    }
}
