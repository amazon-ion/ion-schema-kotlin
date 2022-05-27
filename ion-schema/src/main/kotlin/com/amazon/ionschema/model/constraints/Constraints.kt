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
package com.amazon.ionschema.model.constraints

import com.amazon.ionschema.model.AstConstraint
import com.amazon.ionschema.model.AstType
import com.amazon.ionschema.model.DiscreteRange

/**
 * Common interface for constraints that have range over discrete integers as their only argument.
 * This allows for code reuse when reading, writing, and validating the `scale`, `precision`, and `*_length` constraints.
 */
interface DiscreteRangeConstraint<T> : AstConstraint<T> where T : AstConstraint<T>, T : DiscreteRangeConstraint<T> {
    val range: DiscreteRange
}

/**
 * Common interface for constraints that have a single type as their argument.
 * This allows for code reuse when reading and writing the `element`, `not`, and `type` constraints.
 */
interface UnaryTypeConstraint<T> : AstConstraint<T> where T : AstConstraint<T>, T : UnaryTypeConstraint<T> {
    val type: AstType
}

/**
 * Common interface for constraints that have a list of N types as their only argument.
 * This allows for code reuse when reading and writing the`any_of`, `all_of`, and `one_of` constraints.
 */
interface NAryTypeConstraint<T> : AstConstraint<T> where T : AstConstraint<T>, T : NAryTypeConstraint<T> {
    val types: Iterable<AstType>
}
