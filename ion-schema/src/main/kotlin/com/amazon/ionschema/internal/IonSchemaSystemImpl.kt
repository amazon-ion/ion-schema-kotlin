/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import com.amazon.ion.IonSystem
import com.amazon.ion.IonValue
import com.amazon.ionschema.Authority
import com.amazon.ionschema.IonSchemaException
import com.amazon.ionschema.IonSchemaSystem
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.SchemaCache
import com.amazon.ionschema.internal.util.islRequireNotNull

/**
 * Implementation of [IonSchemaSystem].
 */
internal class IonSchemaSystemImpl(
    override val ionSystem: IonSystem,
    private val authorities: List<Authority>,
    private val constraintFactory: ConstraintFactory,
    private val schemaCache: SchemaCache,
    private val params: Map<Param<out Any>, Any>,
    private val warnCallback: (() -> String) -> Unit
) : IonSchemaSystem {

    private val schemaCores = mapOf(
        IonSchemaVersion.v1_0 to SchemaCore(this, IonSchemaVersion.v1_0),
        IonSchemaVersion.v2_0 to SchemaCore(this, IonSchemaVersion.v2_0)
    )

    internal fun getBuiltInTypesSchema(version: IonSchemaVersion) = schemaCores[version]!!

    private val schemaContentCache = SchemaContentCache(this::loadSchemaContent)

    // Set to be used to detect cycle in import dependencies
    private val schemaImportSet: MutableSet<String> = mutableSetOf()

    override fun loadSchema(id: String): SchemaInternal {
        return usingReferenceManager { loadSchema(it, id) }
    }

    /**
     * Load a [SchemaInternal] using the given [DeferredReferenceManager].
     */
    private fun loadSchema(referenceManager: DeferredReferenceManager, id: String): SchemaInternal {
        val schemaContent = schemaContentCache.getSchemaContent(id)
        return schemaCache.getOrPut(id) { createSchema(referenceManager, schemaContent.version, id, schemaContent.isl) } as SchemaInternal
    }

    /**
     * Loads the [SchemaContent] from the [authorities] for the given [id].
     */
    private fun loadSchemaContent(id: String): SchemaContent {
        val exceptions = mutableListOf<Exception>()
        val schemaIterator = authorities.asSequence().mapNotNull { authority ->
            try {
                val iterator = authority.iteratorFor(this, id)
                if (iterator.hasNext()) {
                    iterator
                } else {
                    iterator.close()
                    null
                }
            } catch (e: Exception) {
                exceptions.add(e)
                null
            }
        }.firstOrNull()

        if (schemaIterator == null) {
            val message = StringBuilder("Unable to resolve schema id '$id'")
            if (exceptions.size > 0) {
                message.append(" ($exceptions)")
            }
            throw IonSchemaException(message.toString())
        }
        return SchemaContent(schemaIterator.use { it.asSequence().toList() })
    }

    /**
     * Invalidates a schema in all caches. Any existing references to the schema instance will still be valid.
     */
    private fun unloadSchema(id: String) {
        schemaCache.invalidate(id)
        schemaContentCache.invalidate(id)
    }

    private fun createSchema(referenceManager: DeferredReferenceManager, version: IonSchemaVersion, schemaId: String?, isl: List<IonValue>): SchemaInternal {
        return when (version) {
            IonSchemaVersion.v1_0 -> SchemaImpl_1_0(referenceManager, this, schemaCores[version]!!, isl.iterator(), schemaId)
            IonSchemaVersion.v2_0 -> SchemaImpl_2_0(referenceManager, this, isl, schemaId)
        }
    }

    /**
     * Constructs a new [DeferredReferenceManager] for this schema system, uses it to run the provided [block], and
     * automatically resolves the reference manager before finally returning the output of [block].
     */
    fun <T> usingReferenceManager(block: (DeferredReferenceManager) -> T): T {
        val referenceManager = DeferredReferenceManager(
            loadSchemaFn = this::loadSchema,
            unloadSchema = this::unloadSchema,
            isSchemaAlreadyLoaded = { schemaCache.getOrNull(it) != null },
            doesSchemaDeclareType = this::doesSchemaDeclareType,
        )
        val t = block(referenceManager)
        referenceManager.resolve()
        return t
    }

    /**
     * Checks if an Ion document exists for the given schema ID. Does not guarantee that the schema is valid ISL—just
     * that there is some Ion that can be found using this schema ID.
     */
    internal fun doesSchemaDocumentExist(schemaId: String): Boolean {
        return schemaContentCache.doesSchemaExist(schemaId)
    }

    /**
     * Checks if the schema document for the given [schemaId] has a named type definition with the name [typeId]. This
     * does not guarantee that the type definition or the schema document are valid—only that a `type::{ name: ... }`
     * exists in the schema document for the given [typeId].
     *
     * Beware—the type might still be in scope in the schema if transitive imports are enabled.
     */
    internal fun doesSchemaDeclareType(schemaId: String, typeId: IonSymbol): Boolean {
        return schemaContentCache.doesSchemaDeclareType(schemaId, typeId)
    }

    /**
     * Returns a list of the names of the types declared in the schema for [schemaId]. If no schema exists for the
     * [schemaId], throws an InvalidSchemaException.
     *
     * This does not guarantee that the type definitions or the schema document are valid—only that a
     * `type::{ name: ... }` exists in the schema document for each [IonSymbol] in the returned list.
     */
    internal fun listDeclaredTypes(schemaId: String): List<IonSymbol> {
        return schemaContentCache.getSchemaContent(schemaId).declaredTypes
    }

    override fun newSchema(): SchemaInternal = newSchema("")

    override fun newSchema(isl: String): SchemaInternal = newSchema(ionSystem.iterate(isl))

    override fun newSchema(isl: Iterator<IonValue>): SchemaInternal {
        val islList = isl.asSequence().toList()

        val version = islList.firstOrNull(IonSchemaVersion::isVersionMarker)
            ?.let {
                islRequireNotNull(IonSchemaVersion.fromIonSymbolOrNull(it as IonSymbol)) { "Unsupported Ion Schema version: $it" }
            }
            ?: IonSchemaVersion.v1_0

        return usingReferenceManager { createSchema(it, version, null, islList) }
    }

    internal fun isConstraint(name: String, schema: SchemaInternal) = constraintFactory.isConstraint(name, schema.ionSchemaLanguageVersion)

    internal fun constraintFor(ion: IonValue, schema: SchemaInternal, referenceManager: DeferredReferenceManager) = constraintFactory.constraintFor(ion, schema, referenceManager)

    internal fun emitWarning(lazyWarning: () -> String) {
        warnCallback.invoke(lazyWarning)
    }

    internal inline fun <reified T : Any> getParam(param: Param<T>): T = params[param] as? T ?: param.defaultValue

    internal sealed class Param<T : Any>(val defaultValue: T) {
        // for backwards compatibility with v1.0
        // Default is to NOT support the backwards compatible behavior
        object ALLOW_ANONYMOUS_TOP_LEVEL_TYPES : Param<Boolean>(false)
        // for backwards compatibility with v1.1
        // Default is to keep the backward compatible behavior
        object ALLOW_TRANSITIVE_IMPORTS : Param<Boolean>(true)
        // Introduced in v1.7.0.
        // This is a performance improvement that skips all other constraints if the
        // annotation doesn't match, regardless of whether the fail-fast was called.
        object SHORT_CIRCUIT_ON_INVALID_ANNOTATIONS : Param<Boolean>(false)
    }
}
