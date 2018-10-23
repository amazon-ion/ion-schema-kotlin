package software.amazon.ionschema

import software.amazon.ion.IonValue
import java.io.Reader

interface Authority {
    fun readerFor(id: String): Reader?
}
