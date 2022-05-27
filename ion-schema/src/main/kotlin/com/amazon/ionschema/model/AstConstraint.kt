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
 * Represents an Ion Schema Language constraint. Implementations must add any necessary properties for modelling the
 * details of that particular constraint.
 *
 * The recursive type bound enforces that [id] has a valueType that is the same type as the implementation of this interface.
 *
 * Sometimes we may need to get a ConstraintId in a static way (i.e. we have no instance of this constraint). For this
 * reason, it is recommended that all concrete implementations of [AstConstraint] include a companion object like this
 * example from [AllOfConstraint]:
 * ```
 * companion object : ConstraintId<AllOfConstraint> by ConstraintId("all_of") {
 *     @JvmField val ID = this@Companion
 * }
 * ```
 * The companion object implements [ConstraintId] which means that when using this library from Kotlin, the class
 * itself is like a static [ConstraintId].
 * ```
 * val someId: ConstraintId<*> = AllOfConstraint
 * ```
 * When using this library from Java, the ConstraintId can be accessed from `AllOfConstraint.ID`
 * ```
 * ConstraintId<?> someId = AllOfConstraint.ID;
 * ```
 */
interface AstConstraint<T : AstConstraint<T>> {
    val id: ConstraintId<T>
}
