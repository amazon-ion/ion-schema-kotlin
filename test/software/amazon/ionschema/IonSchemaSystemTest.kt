package software.amazon.ionschema

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import software.amazon.ion.system.IonSystemBuilder

class IonSchemaSystemTest {
    private class BadAuthorityException : Exception()

    private val exceptionalAuthority = object : Authority {
        override fun iteratorFor(iss: IonSchemaSystem, id: String)
                = throw BadAuthorityException()
    }

    private val ION = IonSystemBuilder.standard().build()

    private val iss = IonSchemaSystemBuilder.standard()
            // mis-configured authority (path should be "data/test"):
            .addAuthority(AuthorityFilesystem("data"))

            // "exceptional" authority:
            .addAuthority(exceptionalAuthority)

            // potentially useful authority:
            .addAuthority(AuthorityFilesystem("data/test"))

            .build()

    @Test(expected = IonSchemaException::class)
    fun unresolvableSchema() {
        iss.loadSchema("")
    }

    @Test
    fun withAuthority() {
        // exceptionalAuthority.iteratorFor() should not be invoked by:
        IonSchemaSystemBuilder.standard()
                .addAuthority(exceptionalAuthority)
                .withAuthority(AuthorityFilesystem("data/test"))
                .build()
                .loadSchema("schema/Customer.isl")
    }

    @Test
    fun withAuthorities() {
        // exceptionalAuthority.iteratorFor() should not be invoked by:
        IonSchemaSystemBuilder.standard()
                .addAuthority(exceptionalAuthority)
                .withAuthorities(listOf<Authority>(AuthorityFilesystem("data/test")))
                .build()
                .loadSchema("schema/Customer.isl")
    }

    @Test
    fun newSchema() {
        val schema = iss.newSchema()
        assertFalse(schema.getTypes().hasNext())
    }

    @Test
    fun newSchema_string() {
        val schema = iss.newSchema("""
                type::{ name: a }
                type::{ name: b }
                type::{ name: c }
                """)
        assertEquals(listOf("a", "b", "c"),
                schema.getTypes().asSequence().toList().map { it.name() })
    }

    @Test
    fun newSchema_iterator() {
        val schema = iss.newSchema(
                ION.iterate("""
                    type::{ name: d }
                    type::{ name: e }
                    type::{ name: f }
                    """))
        assertEquals(listOf("d", "e", "f"),
                schema.getTypes().asSequence().toList().map { it.name() })
    }
}

