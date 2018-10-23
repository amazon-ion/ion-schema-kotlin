package software.amazon.ionschema

import java.io.File
import java.io.FileReader
import java.io.Reader

class AuthorityFilesystem(private val basePath: String) : Authority {
    override fun readerFor(id: String): Reader? {
        val file = File(basePath, id)
        if (file.exists() && file.canRead()) {
            return FileReader(file)
        }
        return null
    }
}
