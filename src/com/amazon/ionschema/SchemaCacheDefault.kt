package com.amazon.ionschema

import java.util.concurrent.ConcurrentHashMap

/**
 * The default SchemaCache implementation, backed by a ConcurrentHashMap.
 */
class SchemaCacheDefault : SchemaCache {
    private val cache = ConcurrentHashMap<String, Schema>()

    override fun getOrPut(key: String, resolver: () -> Schema): Schema
            = cache.getOrPut(key, resolver)

    override fun invalidate(key: String) {
        cache.remove(key)
    }
}

