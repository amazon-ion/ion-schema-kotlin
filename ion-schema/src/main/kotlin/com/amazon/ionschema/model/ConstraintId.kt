/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazon.ionschema.model

/**
 * Constraints are uniquely identified by their name (i.e. their ISL keyword). The [valueType] is included so that it is
 * always possible to safely cast a constraint to the appropriate type, and so that constraint implementations cannot
 * (easily) use the incorrect name or value type.
 *
 * Note—when creating an API, prefer to use [AnyConstraintId] instead of `TypedConstraintId<*>` so that clients of the
 * API can use members of the [KnownConstraintIds] enum.
 */
interface ConstraintId<T : AstConstraint<T>> : AnyConstraintId {
    override val valueType: Class<out AstConstraint<T>>

    companion object {
        /**
         * Factory function for creating instances of [ConstraintId].
         */
        @JvmStatic
        fun <T : AstConstraint<T>> create(constraintName: String, valueType: Class<T>): ConstraintId<T> = ConstraintIdImpl(constraintName, valueType)

        // Kotlin-idiomatic pseudo-constructor. Not callable from Java because of the reified type parameter.
        inline operator fun <reified T : AstConstraint<T>> invoke(constraintId: String) = create(constraintId, T::class.java)
    }
}

private data class ConstraintIdImpl<T : AstConstraint<T>>(override val constraintName: String, override val valueType: Class<T>) : ConstraintId<T>

/**
 * Use this interface instead of `TypedConstraintId<*>`. This interface exists because it is not allowed for an enum
 * class to implement a generic interface.
 *
 * @see ConstraintId
 */
interface AnyConstraintId {
    val constraintName: String
    val valueType: Class<out AstConstraint<*>>
}
