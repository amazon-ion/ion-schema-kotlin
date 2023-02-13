/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import com.amazon.ionschema.Violations

/**
 * Represents a [TypeInternal] that is known to be declared in a schema document, but the [TypeInternal] instance has
 * not necessarily been created yet. A [DeferredReference] can be resolved or unresolved. When it is unresolved,
 * it is unknown whether the referred [TypeInternal] instance exists. When it is resolved, this [DeferredReference]
 * is able to return and/or delegate validation to the referred [TypeInternal] instance.
 */
internal interface DeferredReference : TypeInternal {
    override val isl: IonSymbol
    override val name: String
        get() = isl.stringValue()

    /**
     * Returns the [TypeInternal] that this [DeferredReference] resolves to, or `null` if it cannot be resolved.
     */
    fun resolve(): TypeInternal

    /**
     * Returns true if this [DeferredReference] is already resolved.
     */
    fun isResolved(): Boolean

    override fun getBaseType(): TypeBuiltin = resolve().getBaseType()
    override fun isValidForBaseType(value: IonValue): Boolean = resolve().isValidForBaseType(value)
    override fun validate(value: IonValue, issues: Violations) = resolve().validate(value, issues)
}

/**
 * An implementation of [DeferredReference] that refers to a [TypeInternal] that is declared in a [SchemaInternal]
 * that has already been instantiated.
 *
 * Use this for references to other types that are in scope for the same schema that the reference is being created for.
 */
internal class DeferredLocalReference(
    override val isl: IonSymbol,
    private val schema: SchemaInternal
) : DeferredReference {
    override val schemaId: String? = schema.schemaId
    private var type: TypeInternal? = null

    override fun resolve(): TypeInternal {
        return type
            ?: schema.getType(name)?.also { type = it }
            ?: throw InvalidSchemaException("Unable to resolve type $isl")
    }
    override fun isResolved(): Boolean = type != null
}

/**
 * An implementation of [DeferredReference] that refers to a [TypeInternal] that is declared in a [SchemaInternal]
 * that has not necessarily been instantiated yet. This class also implements [ImportedType], which means that
 * [schemaId] is required to be non-null.
 *
 * Use this for references to other types that are defined in other schemas.
 */
internal class DeferredImportReference constructor(
    override val isl: IonSymbol,
    override val schemaId: String,
    private val schemaProvider: () -> SchemaInternal
) : DeferredReference, ImportedType {
    override val importedFromSchemaId: String
        get() = schemaId

    private var type: TypeInternal? = null

    override fun resolve(): TypeInternal {
        return type
            ?: schemaProvider().getType(name)?.also { type = it }
            ?: throw InvalidSchemaException("Unable to resolve type $isl")
    }
    override fun isResolved(): Boolean = type != null
}
