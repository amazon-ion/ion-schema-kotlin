package com.amazon.ionschema

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonStruct

/**
 * Defines the contract for classes that provide a cache for Schema objects.
 *
 * Implementations of this interface are required to be thread-safe.
 */
interface SchemaCache {
    /**
     * Returns the Schema cached at [key]; if not present, calls the
     * [resolver], caches the resulting Schema, and returns it.  If
     * the [resolver] is unable to return a Schema, it must throw an
     * exception.
     *
     * @param[key] the key of the Schema to retrieve from the cache;
     *     [key] is typically the schema's id
     * @param[resolver] a function that retrieves the Schema to be cached,
     *     if there is no cache entry for [key]
     */
    fun getOrPut(key: String, resolver: () -> Schema): Schema

    /**
     * Returns the Schema cached at [key]; if not present, returns `null`.
     *
     * Implementations of [SchemaCache] _should_ override the default implementation of [getOrNull] with something more
     * sensible. The default implementation exists only for backwards compatibility. It is not particularly efficient.
     *
     * @param[key] the key of the Schema to retrieve from the cache;
     *     [key] is typically the schema's id
     * @since v1.5.0
     */
    fun getOrNull(key: String): Schema? {
        val schema = getOrPut(key) { UnimplementedSchema }
        return if (schema == UnimplementedSchema) {
            invalidate(key)
            null
        } else {
            schema
        }
    }

    /**
     * Invalidates the cache entry at [key], if one exists.
     *
     * @param[key] the cache key to invalidate
     */
    fun invalidate(key: String)
}

private object UnimplementedSchema : Schema {
    override val isl: IonDatagram
        get() = TODO("Intentionally unimplemented")
    override fun getImport(id: String): Import = TODO("Intentionally unimplemented")
    override fun getImports(): Iterator<Import> = TODO("Intentionally unimplemented")
    override fun getType(name: String): Type = TODO("Intentionally unimplemented")
    override fun getTypes(): Iterator<Type> = TODO("Intentionally unimplemented")
    override fun getDeclaredType(name: String): Type = TODO("Intentionally unimplemented")
    override fun getDeclaredTypes(): Iterator<Type> = TODO("Intentionally unimplemented")
    override fun getSchemaSystem(): IonSchemaSystem = TODO("Intentionally unimplemented")
    override fun newType(isl: String): Type = TODO("Intentionally unimplemented")
    override fun newType(isl: IonStruct): Type = TODO("Intentionally unimplemented")
    override fun plusType(type: Type): Schema = TODO("Intentionally unimplemented")
    override val ionSchemaLanguageVersion: IonSchemaVersion
        get() = TODO("Intentionally unimplemented")
}
