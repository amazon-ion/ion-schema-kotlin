package com.amazon.ionschema

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonValue
import com.amazon.ionschema.util.CloseableIterator

/**
 * An Authority implementation that is backed by an in-memory map of [IonValue]s or Ion Text [String]s.
 */
class InMemoryMapAuthority private constructor(
    private val schemaText: Map<String, String> = mapOf(),
    private val schemas: MutableMap<String, IonDatagram> = mutableMapOf()
) : Authority {
    companion object {
        /**
         * Constructs an instance of [InMemoryMapAuthority] from a map of Ion Text strings, which
         * are lazily materialized to the [IonValue] DOM.
         */
        @JvmStatic
        fun fromIonText(schemaText: Map<String, String>) =
            InMemoryMapAuthority(schemaText = schemaText)

        /**
         * Constructs an instance of [InMemoryMapAuthority] from pairs of schemaIds and Ion Text strings, which
         * are lazily materialized to the [IonValue] DOM.
         */
        fun fromIonText(vararg schemaText: Pair<String, String>) = fromIonText(mapOf(*schemaText))

        /**
         * Constructs an instance of [InMemoryMapAuthority] from a map of [IonDatagram]s.
         */
        @JvmStatic
        fun fromIonValues(schemas: Map<String, IonDatagram>) =
            InMemoryMapAuthority(schemas = schemas.toMutableMap())

        /**
         * Constructs an instance of [InMemoryMapAuthority] from pairs of schemaIds and [IonDatagram]s.
         */
        fun fromIonValues(vararg schemas: Pair<String, IonDatagram>) = fromIonValues(mapOf(*schemas))
    }

    override fun iteratorFor(iss: IonSchemaSystem, id: String): CloseableIterator<IonValue> {
        if (!schemas.containsKey(id) && schemaText.containsKey(id)) {
            schemas[id] = iss.ionSystem.loader.load(schemaText[id])
        }
        return schemas[id]?.iterator()?.asCloseableIterator() ?: EMPTY_ITERATOR
    }

    /**
     * Wraps a regular [Iterator] in a [CloseableIterator] with a no-op
     * implementation of [CloseableIterator.close]. This may be a useful
     * public function, but it has the potential to be misused on iterators
     * of resources that DO need to be closed, so this function is private
     * for the time being.
     */
    private fun <T> Iterator<T>.asCloseableIterator(): CloseableIterator<T> {
        return object : CloseableIterator<T>, Iterator<T> by this {
            override fun close() = Unit
        }
    }
}
