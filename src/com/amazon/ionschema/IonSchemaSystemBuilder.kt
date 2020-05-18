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
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.internal.ConstraintFactoryDefault
import com.amazon.ionschema.internal.IonSchemaSystemImpl

/**
 * Entry point for Ion Schema.  Provides a builder API for constructing
 * [IonSchemaSystem]s using the specified [Authority]s and IonSystem.
 */
class IonSchemaSystemBuilder private constructor() {
    companion object {
        /**
         * Provides a standard instance of IonSchemaSystemBuilder.
         */
        @JvmStatic
        fun standard() = IonSchemaSystemBuilder()

        private val defaultConstraintFactory = ConstraintFactoryDefault()
    }

    private var authorities = mutableListOf<Authority>()
    private var constraintFactory = defaultConstraintFactory
    private var ionSystem = IonSystemBuilder.standard().build()
    private var schemaCache: SchemaCache? = null

    /**
     * Adds the provided authority to the list of [Authority]s.
     */
    fun addAuthority(authority: Authority): IonSchemaSystemBuilder {
        authorities.add(authority)
        return this
    }

    /**
     * Replaces the list of [Authority]s with a list containing only
     * the specified authority.
     */
    fun withAuthority(authority: Authority): IonSchemaSystemBuilder {
        this.authorities = mutableListOf(authority)
        return this
    }

    /**
     * Replaces the list of [Authority]s with the specified list of [Authority]s.
     */
    fun withAuthorities(authorities: List<Authority>): IonSchemaSystemBuilder {
        this.authorities = mutableListOf<Authority>().apply { addAll(authorities) }
        return this
    }

    /**
     * Provides the IonSystem to use when building an IonSchemaSystem.
     */
    fun withIonSystem(ionSystem: IonSystem): IonSchemaSystemBuilder {
        this.ionSystem = ionSystem
        return this
    }

    /**
     * Provides a SchemaCache to use when building an IonSchemaSystem.
     */
    fun withSchemaCache(schemaCache: SchemaCache): IonSchemaSystemBuilder {
        this.schemaCache = schemaCache
        return this
    }

    /**
     * Instantiates an [IonSchemaSystem] using the provided [Authority](s)
     * and IonSystem.
     */
    fun build(): IonSchemaSystem = IonSchemaSystemImpl(
            ionSystem,
            authorities,
            constraintFactory,
            schemaCache ?: SchemaCacheDefault()
    )
}

