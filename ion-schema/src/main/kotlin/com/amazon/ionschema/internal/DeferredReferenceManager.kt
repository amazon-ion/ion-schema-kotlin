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
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Schema
import java.util.LinkedList
import java.util.Queue

/**
 * A single-use "session" for creating, tracking, and resolving deferred references _for multiple [Schema]_.
 *
 * A [DeferredReferenceManager] allows you to create [DeferredReference] instances, and it keeps track of any
 * [DeferredReference] instances that it creates. When this [DeferredReferenceManager] is closed, it attempts to
 * resolve all of the [DeferredReference]. If any cannot be resolved, it throws an [InvalidSchemaException] listing
 * the types that cannot be resolved.
 *
 * A [DeferredReferenceManager] ensures that all [DeferredReference]s it creates refer to a type that does actually
 * exist in the source ISL. (Caveat—it can't yet do that for local references in anonymous schemas.) This check is not
 * strictly necessary since the non-existence of the type would be discovered when attempting to resolve the type, but
 * including this check does help to reason about the correctness of the implementation. If it is discovered to be a
 * performance bottleneck, it could safely be removed.
 *
 * In addition, a schema can be registered with the [DeferredReferenceManager] as being dependent on the outcome of
 * resolving the deferred references. See [registerDependentSchema] for details.
 *
 * Once a [DeferredReferenceManager] instance has been closed, you cannot use that instance to create any new references
 * nor can you register any dependent schemas, and the instance should be discarded. Attempting to perform any operation
 * once closed will result in an [IllegalStateException] being thrown.
 */
@OptIn(DeferredReferenceManagerImplementationDetails::class)
internal class DeferredReferenceManager(
    private val loadSchemaFn: (DeferredReferenceManager, String) -> SchemaInternal,
    private val unloadSchema: (String) -> Unit,
    private val isSchemaAlreadyLoaded: (String) -> Boolean,
    private val doesSchemaDeclareType: (String, IonSymbol) -> Boolean,
) {

    private val deferredTypeReferences: Queue<DeferredReference> = LinkedList()
    private val dependentSchemas = mutableSetOf<String>()
    private var isClosed = false

    /**
     * Creates a [DeferredLocalReference] for a type with name [typeId] that is in scope for the given [schema].
     */
    fun createDeferredLocalReference(schema: SchemaInternal, typeId: IonSymbol): TypeInternal {
        if (isClosed) throw IllegalStateException("Cannot create a new reference using a closed DeferredReferenceManager")
        // TODO: See if it's possible to enforce this guarantee for anonymous schemas
        schema.schemaId?.let { schemaId ->
            if (!doesSchemaDeclareType(schemaId, typeId)) throw InvalidSchemaException("No type named '$typeId' in schema $schemaId")
        }
        val ref = DeferredLocalReference(typeId, schema)
        deferredTypeReferences.add(ref)
        return ref
    }

    /**
     * Creates an [DeferredImportReference] for a type with name [typeId] that is declared in [schemaId].
     */
    fun createDeferredImportReference(schemaId: String, typeId: IonSymbol): ImportedType {
        if (isClosed) throw IllegalStateException("Cannot create a new reference using a closed DeferredReferenceManager")
        if (!doesSchemaDeclareType(schemaId, typeId)) throw InvalidSchemaException("No type named '$typeId' in schema $schemaId")
        val ref = DeferredImportReference(typeId, schemaId) { loadSchemaFn(this, schemaId) }
        deferredTypeReferences.add(ref)
        return ref
    }

    /**
     * Indicates that the validity of the [Schema] for the given [schemaId] depends on the outcome of this
     * [DeferredReferenceManager]. If the [Schema] for the given [schemaId] is already loaded into the [IonSchemaSystemImpl],
     * then this function does nothing. Otherwise, the [schemaId] is added to the list of [dependentSchemas] that will
     * need to be invalidated if any deferred references cannot be resolved.
     *
     * In other words, we cannot guarantee that a schema is valid until we have validated all of its deferred references.
     * If any references are deferred, and then later discovered to be invalid, then we must invalidate all schemas that
     * depend on that reference—directly or indirectly. In practice, however, this class takes a safe-but-naive approach
     * and will unload *all* schemas that are dependent on the outcome of this [DeferredReferenceManager].
     *
     * This function should only be called from the initializer block of a [Schema] implementation.
     */
    internal fun registerDependentSchema(schemaId: String) {
        if (isClosed) throw IllegalStateException("Cannot register a dependent schema with a closed DeferredReferenceManager")

        // If this function is only called from the init block of a Schema implementation (as prescribed), then it
        // should not be possible for the given schemaId to already be loaded. However, we check anyway so that this
        // function does not have to make assumptions about its preconditions.
        if (isSchemaAlreadyLoaded(schemaId)) return

        dependentSchemas.add(schemaId)
    }

    fun resolve() {
        if (isClosed) throw IllegalStateException("Cannot call resolve() on a closed DeferredReferenceManager")
        try {

            // NOTE: we must use a loop rather than an iterator/stream/sequence since more items can be
            // added to the queue as a side effect of resolving a deferred type reference.
            while (deferredTypeReferences.isNotEmpty()) {
                deferredTypeReferences.poll().resolve()
            }
        } catch (e: Exception) {
            // If anything goes wrong, evict all possibly bad schemas that might have been cached
            dependentSchemas.forEach { unloadSchema(it) }
            throw e
        } finally {
            isClosed = true
        }
    }
}
