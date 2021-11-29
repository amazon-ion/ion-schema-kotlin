package com.amazon.ionschema

import com.amazon.ion.IonValue
import com.amazon.ionschema.util.CloseableIterator
import java.io.Closeable
import java.io.File
import java.io.InputStream

/**
 * An [Authority] implementation that attempts to resolve schema ids to resources
 * in a [ClassLoader]'s classpath.
 *
 * @property[rootPackage] The base path within the [ClassLoader]'s classpath in which
 *     to resolve schema identifiers.
 * @property[classLoader] The [ClassLoader] to use to find the schema resources.
 */
class ResourceAuthority(
    private val rootPackage: String,
    private val classLoader: ClassLoader
) : Authority {

    override fun iteratorFor(iss: IonSchemaSystem, id: String): CloseableIterator<IonValue> {
        val resourcePath = File(rootPackage, id).toPath().normalize().toString()
        if (!resourcePath.startsWith(rootPackage)) {
            throw AccessDeniedException(File(id))
        }
        val stream: InputStream = classLoader.getResourceAsStream(resourcePath) ?: return EMPTY_ITERATOR

        val ion = iss.ionSystem
        val reader = ion.newReader(stream)

        return object : CloseableIterator<IonValue>, Iterator<IonValue> by ion.iterate(reader), Closeable by reader {
            // Intentionally empty body because this object has all its methods implemented by delegation.
        }
    }

    companion object {
        /**
         * Factory method for constructing a [ResourceAuthority] that can access the schemas provided by
         * [`ion-schema-schemas`](https://github.com/amzn/ion-schema-schemas/).
         */
        @JvmStatic
        fun forIonSchemaSchemas(): ResourceAuthority = ResourceAuthority("ion-schema-schemas", ResourceAuthority::class.java.classLoader)
    }
}
