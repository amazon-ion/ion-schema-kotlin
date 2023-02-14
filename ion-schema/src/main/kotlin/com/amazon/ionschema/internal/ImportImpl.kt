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

import com.amazon.ionschema.Import
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Type

/**
 * Implementation of [Import] for all user-provided ISL.
 *
 * This is no longer used to support any of the internal functionality of Ion Schema Kotlin. It exists only for the
 * [Schema.getImports] and [Schema.getImport] functions.
 *
 * Do not call any functions of this class (directly or indirectly) from within the init block of _any_ [Schema]
 * implementation. If you do, the call may fail, depending on the order that schema imports are resolved, which is not
 * guaranteed to be stable.
 *
 * This class (and [Import]) should be removed in v2.0.0 in favor of a map of schemaId to [Type] (or similar).
 */
internal class ImportImpl(
    override val id: String,
    private val schemaProvider: () -> Schema,
    private val types: Map<String, Type>
) : Import {

    override fun getSchema() = schemaProvider()

    override fun getType(name: String) = types[name]

    override fun getTypes(): Iterator<Type> = types.values.iterator()
}
