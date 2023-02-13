/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.ionschema.internal

import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException

/**
 * A class that caches the uninterpreted content of a Schema document. This enables us to determine which types are
 * declared in a schema prior to having a [SchemaInternal] instance for the schema, which is especially useful for
 * checking the validity of a type reference without having to resolve the type reference to a [TypeInternal] instance.
 */
internal class SchemaContentCache constructor(
    private val loader: (String) -> SchemaContent,
    private val delegate: MutableMap<String, SchemaContent> = mutableMapOf(),
) {
    constructor(
        /**
         * A function to look up a schema that has already been loaded by the [IonSchemaSystemImpl] so that we can ensure
         * that this [SchemaContentCache] has a consistent view of a schema, and so that it doesn't do unnecessary work to
         * load a schema document that has already been loaded e.g. from disk.
         */
        getPreloadedSchemaIsl: (String) -> List<IonValue>?,
        /**
         * A function to load the schema document content for a schemaId (most likely by using authorities).
         */
        loadSchemaIslForId: (String) -> List<IonValue>
    ) : this({ SchemaContent(getPreloadedSchemaIsl(it) ?: loadSchemaIslForId(it)) })

    /**
     * Returns the [SchemaContent] for the given [schemaId]. Throws an [InvalidSchemaException] if the schema is not
     * found or is malformed Ion.
     */
    fun getSchemaContent(schemaId: String): SchemaContent {
        return delegate.getOrPut(schemaId) {
            try {
                loader(schemaId)
            } catch (e: Exception) {
                throw InvalidSchemaException("Unable to load schema $schemaId: ${e.message}")
            }
        }
    }

    /**
     * Checks if a schema exists for the given schema ID. Does not guarantee that the schema is valid ISL—just that
     * there is some Ion that can be found using this schema ID.
     *
     * Has the side effect of loading the content for the given [schemaId] if that content has not already been loaded
     * into this instance of [SchemaContentCache].
     */
    fun doesSchemaExist(schemaId: String): Boolean {
        return runCatching { getSchemaContent(schemaId) }.isSuccess
    }

    /**
     * Checks if the schema document for the given [schemaId] has a named type definition with the name [typeId]. This
     * does not guarantee that the type definition or the schema document are valid—only that a `type::{ name: ... }`
     * exists in the schema document for the given [typeId].
     *
     * Has the side effect of loading the content for the given [schemaId] if that content has not already been loaded
     * into this instance of [SchemaContentCache].
     */
    fun doesSchemaDeclareType(schemaId: String, typeId: IonSymbol): Boolean {
        return runCatching { typeId in getSchemaContent(schemaId).declaredTypes }.getOrElse { false }
    }

    /**
     * Removes a schema from this cache.
     */
    fun invalidate(schemaId: String) {
        delegate.remove(schemaId)
    }
}
