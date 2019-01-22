package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.util.CloseableIterator
import java.io.File
import java.io.FileReader
import java.io.Reader

class AuthorityFilesystem(private val basePath: String) : Authority {
    override fun iteratorFor(iss: IonSchemaSystem, id: String): CloseableIterator<IonValue> {
        val file = File(basePath, id)
        if (file.exists() && file.canRead()) {
            return object : CloseableIterator<IonValue> {
                private var reader: FileReader? = FileReader(file)
                private val iter = iss.getIonSystem().iterate(reader)

                override fun hasNext() = iter.hasNext()
                override fun next() = iter.next()
                override fun close() {
                    try {
                        reader?.let(Reader::close)
                    } finally {
                        reader = null
                    }
                }
            }
        }
        return EMPTY_ITERATOR
    }
}

