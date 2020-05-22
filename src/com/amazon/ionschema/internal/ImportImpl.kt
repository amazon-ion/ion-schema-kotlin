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
import java.lang.IllegalArgumentException

/**
 * Implementation of [Import] for all user-provided ISL.
 */
internal class ImportImpl(
        override val id: String,
        private val schema: Schema,
        private val importEntireSchema: Boolean,
        private val types: Map<String, Type>?
) : Import {

    init {
        when {
            !importEntireSchema && (types == null || types.isEmpty()) ->
                throw IllegalArgumentException(
                        "types to import must be provided when importEntireSchema is false")
        }
    }

    override fun getSchema() = schema

    override fun getType(name: String): Type? = if (importEntireSchema) {
        schema.getType(name) ?: types?.get(name)
    } else {
        types?.get(name)
    }

    override fun getTypes(): Iterator<Type> = if (importEntireSchema) {
        (schema.getTypes().asSequence() + (types?.values?.asSequence() ?: emptySequence())).iterator()
    } else {
        types!!.values.iterator()
    }
}

