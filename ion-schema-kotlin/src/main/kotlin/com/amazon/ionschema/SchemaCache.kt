package com.amazon.ionschema

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
     * Invalidates the cache entry at [key], if one exists.
     *
     * @param[key] the cache key to invalidate
     */
    fun invalidate(key: String)
}
