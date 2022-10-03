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

import com.amazon.ion.IonSystem
import com.amazon.ion.IonValue
import com.amazon.ionschema.Authority
import com.amazon.ionschema.IonSchemaException
import com.amazon.ionschema.IonSchemaSystem
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Schema
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
    )

    // Set to be used to detect cycle in import dependencies
    private val schemaImportSet: MutableSet<String> = mutableSetOf()

    override fun loadSchema(id: String): Schema {
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

        return schemaIterator.use { schemaCache.getOrPut(id) { SchemaImpl(this, schemaCores, it, id) } }
    }

    override fun newSchema() = newSchema("")

    override fun newSchema(isl: String) = newSchema(ionSystem.iterate(isl))

    override fun newSchema(isl: Iterator<IonValue>) = SchemaImpl(this, schemaCores, isl, null)

    internal fun isConstraint(name: String, schema: Schema) = constraintFactory.isConstraint(name, schema.ionSchemaLanguageVersion)

    internal fun constraintFor(ion: IonValue, schema: Schema) = constraintFactory.constraintFor(ion, schema)

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
