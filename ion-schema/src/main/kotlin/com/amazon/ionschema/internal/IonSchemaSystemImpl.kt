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
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaException
import com.amazon.ionschema.IonSchemaSystem
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.SchemaCache

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

    // Set to be used to detect cycle in import dependencies
    private val schemaImportSet: MutableSet<String> = mutableSetOf()

    override fun loadSchema(id: String): SchemaInternal {
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

        var version = IonSchemaVersion.v1_0
        val isl = schemaIterator.use {
            it.asSequence()
                .onEach {
                    if (it is IonSymbol && IonSchemaVersion.VERSION_MARKER_REGEX.matches(it.stringValue())) {
                        version = IonSchemaVersion.fromIonSymbolOrNull(it)
                            ?: throw InvalidSchemaException("Unsupported Ion Schema version: $it")
                    }
                }
                .toList()
        }

        return schemaCache.getOrPut(id) { createSchema(version, id, isl) } as SchemaInternal
    }

    private fun createSchema(version: IonSchemaVersion, schemaId: String?, isl: List<IonValue>): SchemaInternal {
        return when (version) {
            IonSchemaVersion.v1_0 -> SchemaImpl_1_0(this, schemaCores[version]!!, isl.iterator(), schemaId)
            IonSchemaVersion.v2_0 -> SchemaImpl_2_0(this, schemaCores[version]!!, isl.iterator(), schemaId)
        }
    }

    override fun newSchema(): SchemaInternal = newSchema("")

    override fun newSchema(isl: String): SchemaInternal = newSchema(ionSystem.iterate(isl))

    override fun newSchema(isl: Iterator<IonValue>): SchemaInternal {
        var version = IonSchemaVersion.v1_0
        val islList = isl.asSequence()
            .onEach {
                if (it is IonSymbol && IonSchemaVersion.VERSION_MARKER_REGEX.matches(it.stringValue())) {
                    version = IonSchemaVersion.fromIonSymbolOrNull(it) ?: throw InvalidSchemaException("Unsupported Ion Schema version: $it")
                }
            }
            .toList()
        return createSchema(version, null, islList)
    }

    internal fun isConstraint(name: String, schema: SchemaInternal) = constraintFactory.isConstraint(name, schema.ionSchemaLanguageVersion)

    internal fun constraintFor(ion: IonValue, schema: SchemaInternal) = constraintFactory.constraintFor(ion, schema)

    internal fun getSchemaImportSet() = schemaImportSet

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
    }
}
