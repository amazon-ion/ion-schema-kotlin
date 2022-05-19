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

package com.amazon.ionschema

import com.amazon.ion.IonSystem
import com.amazon.ion.IonValue

/**
 * Provides methods for instantiating instances of [Schema].
 *
 * To create an instance, use [IonSchemaSystemBuilder].
 */
interface IonSchemaSystem {

    /**
     * The [IonSystem] instance that is being used by this [IonSchemaSystem].
     *
     * In general, [IonValue] instances returned from one `IonSystem` instance
     * are not interoperable with those returned by other instances. (See
     * documentation for [IonSystem] for more details.)
     *
     * This property is an implementation detail, but it is intentionally
     * exposed so that consumers of this library can conveniently ensure
     * they are using the same `IonSystem` instance as the `IonSchemaSystem`.
     */
    val ionSystem: IonSystem

    /**
     * Requests each of the provided [Authority]s, in order, to resolve
     * the requested schema id until one successfully resolves it.
     *
     * If an Authority throws an exception, resolution silently proceeds
     * to the next Authority.
     *
     * @param[id] Identifier for the schema to load.
     * @throws IonSchemaException if the schema id cannot be resolved.
     * @throws InvalidSchemaException if the schema, once resolved, is determined to be invalid.
     */
    fun loadSchema(id: String): Schema

    /**
     * Constructs a new, empty schema.
     *
     * @return the new schema
     */
    fun newSchema(): Schema

    /**
     * Constructs a new schema using ISL provided as a String.
     *
     * @param[isl] ISL string representation of the schema to create
     * @return the new schema
     */
    fun newSchema(isl: String): Schema

    /**
     * Constructs a new schema using ISL provided as Iterator<IonValue>.
     *
     * @param[isl] Iterator<IonValue> representing the desired schema
     * @return the new schema
     */
    fun newSchema(isl: Iterator<IonValue>): Schema
}
