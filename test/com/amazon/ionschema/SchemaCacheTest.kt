package com.amazon.ionschema

import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.util.CloseableIterator
import org.junit.Assert.*
import org.junit.Test

private val ION = IonSystemBuilder.standard().build()
private val isl = "type::{name: a, type: symbol, codepoint_length: 3}"
private val schemaId = "test_schema"

class SchemaCacheTest {
    private val authority = CustomAuthority()

    @Test
    fun defaultCache() {
        val iss = IonSchemaSystemBuilder.standard()
                .addAuthority(authority)
                .build()

        val schema = iss.loadSchema(schemaId)
        checkSchema(schema)
        val schemaFromCache = iss.loadSchema(schemaId)
        assertTrue(schema == schemaFromCache)
    }

    @Test
    fun schemaCacheDefault_invalidate() {
        val schemaCache = SchemaCacheDefault()

        val iss = IonSchemaSystemBuilder.standard()
                .addAuthority(authority)
                .withSchemaCache(schemaCache)
                .build()

        val schema = iss.loadSchema(schemaId)
        checkSchema(schema)
        val schemaFromCache = iss.loadSchema(schemaId)
        assertTrue(schema == schemaFromCache)

        schemaCache.invalidate(schemaId)

        val schemaAfterInvalidation = iss.loadSchema(schemaId)
        assertTrue(schema != schemaAfterInvalidation)
        checkSchema(schemaAfterInvalidation)
    }

    @Test
    fun noopCache() {
        val noopCache = object : SchemaCache {
            override fun getOrPut(key: String, resolver: () -> Schema) = resolver()
            override fun invalidate(key: String) {}
        }

        val iss = IonSchemaSystemBuilder.standard()
                .addAuthority(authority)
                .withSchemaCache(noopCache)
                .build()

        val schema = iss.loadSchema(schemaId)
        checkSchema(schema)
        val schema2 = iss.loadSchema(schemaId)
        checkSchema(schema2)
        assertTrue(schema != schema2)
    }

    private class CustomAuthority : Authority {
        override fun iteratorFor(iss: IonSchemaSystem,
                                 id: String): CloseableIterator<IonValue> =

            object : CloseableIterator<IonValue> {
                private val iter = ION.iterate(isl)

                override fun hasNext(): Boolean = iter.hasNext()
                override fun next(): IonValue = iter.next()
                override fun close() {}
            }
    }

    private fun checkSchema(schema: Schema) {
        val type = schema.getType("a")!!
        assertFalse(type.isValid(ION.singleValue("ab")))
        assertTrue (type.isValid(ION.singleValue("abc")))
        assertFalse(type.isValid(ION.singleValue("abcd")))
    }
}

