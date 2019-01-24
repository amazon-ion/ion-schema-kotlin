package software.amazon.ionschema

import org.junit.Test

class IonSchemaSystemTest {
    private class BadAuthorityException : Exception()

    private val exceptionalAuthority = object : Authority {
        override fun iteratorFor(iss: IonSchemaSystem, id: String)
                = throw BadAuthorityException()
    }

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
    fun withAuthorityTest() {
        // exceptionalAuthority.iteratorFor() should not be invoked by:
        IonSchemaSystemBuilder.standard()
                .addAuthority(exceptionalAuthority)
                .withAuthority(AuthorityFilesystem("data/test"))
                .build()
                .loadSchema("schema/Customer.isl")
    }

    @Test
    fun withAuthoritiesTest() {
        // exceptionalAuthority.iteratorFor() should not be invoked by:
        IonSchemaSystemBuilder.standard()
                .addAuthority(exceptionalAuthority)
                .withAuthorities(listOf<Authority>(AuthorityFilesystem("data/test")))
                .build()
                .loadSchema("schema/Customer.isl")
    }
}

