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

import com.amazon.ionschema.Schema

/**
 * Provides some internal-only members of schema implementations.
 * Also, overrides some members of [Schema] to have internal-only return types.
 *
 * All implementations of a schema must implement [SchemaInternal].
 */
internal interface SchemaInternal : Schema {
    val schemaId: String?

    /**
     * The in-scope types for a schema consist of the ISL core types, any types declared in that schema, and any types
     * that are imported to the schema. This function is intended for internal use only in order to detect naming
     * conflicts and to handle type references that are just a type name.
     */
    fun getInScopeType(name: String): TypeInternal?

    // These are all present in [Schema], but are being overridden to have the internal return type.
    override fun getDeclaredType(name: String): TypeInternal?
    override fun getDeclaredTypes(): Iterator<TypeInternal>
    override fun getType(name: String): TypeInternal?
    override fun getTypes(): Iterator<TypeInternal>
    override fun getSchemaSystem(): IonSchemaSystemImpl
}
