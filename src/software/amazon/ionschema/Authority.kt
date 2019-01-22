package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.util.CloseableIterator

interface Authority {
    fun iteratorFor(iss: IonSchemaSystem, id: String): CloseableIterator<IonValue>
}

val EMPTY_ITERATOR = object : CloseableIterator<IonValue> {
    override fun hasNext() = false
    override fun next(): IonValue = throw NoSuchElementException()
    override fun close() { }
}

