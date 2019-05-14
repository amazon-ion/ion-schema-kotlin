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

package software.amazon.ionschema.internal

import software.amazon.ion.IonSystem
import software.amazon.ion.IonValue
import software.amazon.ionschema.Authority
import software.amazon.ionschema.IonSchemaException
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema

/**
 * Implementation of [IonSchemaSystem].
 */
internal class IonSchemaSystemImpl(
        private val ION: IonSystem,
        private val authorities: List<Authority>,
        private val constraintFactory: ConstraintFactory
) : IonSchemaSystem {

    private val schemaCore = SchemaCore(this)
    private val schemaCache = mutableMapOf<String, Schema>()

    override fun loadSchema(id: String) =
        schemaCache.getOrPut(id, {
            val exceptions = mutableListOf<Exception>()
            authorities.forEach { authority ->
                try {
                    authority.iteratorFor(this, id).use {
                        if (it.hasNext()) {
                            return SchemaImpl(this, schemaCore, it)
                        }
                    }
                } catch (e: Exception) {
                    exceptions.add(e)
                }
            }

            val message = StringBuilder("Unable to resolve schema id '$id'")
            if (exceptions.size > 0) {
                message.append(" ($exceptions)")
            }
            throw IonSchemaException(message.toString())
        })

    override fun newSchema() = newSchema("")

    override fun newSchema(isl: String) = newSchema(ION.iterate(isl))

    override fun newSchema(isl: Iterator<IonValue>) = SchemaImpl(this, schemaCore, isl)

    internal fun isConstraint(name: String)
            = constraintFactory.isConstraint(name)

    internal fun constraintFor(ion: IonValue, schema: Schema)
            = constraintFactory.constraintFor(ion, schema)

    internal fun getIonSystem() = ION
}

