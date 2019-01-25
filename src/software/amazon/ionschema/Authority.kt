package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.util.CloseableIterator

/**
 * An Authority is responsible for resolving schema identifiers.
 *
 * The structure of a schema identifier string is defined by the
 * Authority responsible for the schema/type(s) being imported.
 *
 * Note that runtime resolution of a schema over a network presents
 * availability and security risks, and should thereby be avoided.
 *
 * @see AuthorityFilesystem
 */
interface Authority {
    /**
     * Provides a CloseableIterator<IonValue> for the requested schema identifier.
     * If an error condition is encountered while attempting to resolve the schema
     * identifier, this method should throw an exception.  If no error conditions
     * were encountered, but the schema identifier can't be resolved, this method
     * should return EMPTY_ITERATOR.
     *
     * @see EMPTY_ITERATOR
     */
    fun iteratorFor(iss: IonSchemaSystem, id: String): CloseableIterator<IonValue>
}

/**
 * A singleton iterator which has nothing to iterate over.
 */
val EMPTY_ITERATOR = object : CloseableIterator<IonValue> {
    override fun hasNext() = false
    override fun next(): IonValue = throw NoSuchElementException()
    override fun close() { }
}

