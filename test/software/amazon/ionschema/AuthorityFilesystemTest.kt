package software.amazon.ionschema

import org.junit.Test

class AuthorityFilesystemTest {
    @Test(expected = IllegalArgumentException::class)
    fun nonExistentPath() {
        AuthorityFilesystem("non-existent-path")
    }
}

