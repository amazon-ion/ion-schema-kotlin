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

import com.amazon.ion.IonValue
import com.amazon.ionschema.Type

/**
 * Internal methods for interacting with [Type]s.
 */
internal interface TypeInternal : Type, Constraint {

    /**
     * The name of the schemaId that this type was defined in.
     */
    val schemaId: String?

    @Deprecated("Only used for Ion Schema 1.0 code paths. No new usages should be introduced.")
    fun getBaseType(): TypeBuiltin

    @Deprecated("Only used for Ion Schema 1.0 code paths. No new usages should be introduced.")
    fun isValidForBaseType(value: IonValue): Boolean
}

/**
 * Represents a type that was imported from another schema.
 */
internal interface ImportedType : TypeInternal {
    val importedFromSchemaId: String

    /**
     * The name of the schemaId that this type was defined in.
     * Unlike [TypeInternal], this is always non-null since we
     * can't import types from a schema if that schema has no id.
     */
    override val schemaId: String
}

/**
 * The name of the schemaId that this type was defined in.
 *
 * Even though it is not part of the public API, it is convenient to have [schemaId] available on [Type] internally.
 */
internal val Type.schemaId: String?
    get() = (this as? TypeInternal)?.schemaId
