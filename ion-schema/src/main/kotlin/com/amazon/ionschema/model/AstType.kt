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

import com.amazon.ionelement.api.*

sealed class AstType {

    /**
     * Represents a named reference to another type. If the [typeId] exists in the scope of the enclosing type (i.e. the
     * type was imported in the header, declared elsewhere in the same schema document, or is a built-in type), then
     * [schemaId] can be null. For inline imports, [schemaId] must not be null.
     */
    data class TypeReference @JvmOverloads constructor(val typeId: String, val schemaId: String? = null) : AstType()

    /**
     * Represents a type as defined by a set of constraints, with optional user-provided content (i.e. "open content").
     */
    data class TypeDefinition @JvmOverloads constructor(
        val constraints: Collection<AstConstraint<*>>,
        val userContent: List<Pair<String, IonElement>> = emptyList()
    ) : AstType()
}

/**
 * Gets all instances of a particular constraint, cast to the correct type for the given [constraintId].
 */
operator fun <T : AstConstraint<T>> AstType.TypeDefinition.get(constraintId: ConstraintId<T>): List<T> {
    // Compiler thinks that this is an unchecked cast, but we know it's safe because the type bound of AstConstraint
    // and ConstraintId must be the same, and we've already verified that the ID matches.
    return constraints.filter { it.id == constraintId }.map { it as T }
}
