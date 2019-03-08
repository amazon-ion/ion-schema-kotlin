package software.amazon.ionschema

import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Test

class AuthorityFilesystemTest {
    @Test(expected = IllegalArgumentException::class)
    fun nonExistentPath() {
        AuthorityFilesystem("non-existent-path")
    }

    @Test
    fun unknownSchemaId() {
        val iss = IonSchemaSystemBuilder.standard().build()
        val authority = AuthorityFilesystem("data/test")
        val iter = authority.iteratorFor(iss, "unknown_schema_id")
        assertFalse(iter.hasNext())
        try {
            iter.next()
            fail()
        } catch (e: NoSuchElementException) {
        }
        iter.close()
    }
}

