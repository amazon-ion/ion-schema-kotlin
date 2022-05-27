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
 * Interactions between the Ion Schema System and [AstConstraint]s are mediated through ConstraintMediators.
 *
 * A ConstraintMediator can hold any kind of data associated to [ConstraintId]s, but it is intended primarily for holding
 * things such as reader, writer, and validator functions where we need preserve some type information that would
 * normally be lost through type-erasure.
 */
abstract class ConstraintMediator<R : ConstraintMediator.Registration<*>>(private val registrations: List<R>) {
    /**
     * Tests whether the mediator contains any registration for the given constraint name.
     */
    operator fun contains(name: String): Boolean = registrations.any { it.id.constraintName == name }

    /**
     * Gets the wild-card-typed registration for the given constraint name.
     */
    operator fun get(name: String): R = registrations.single { it.id.constraintName == name }

    /**
     * Subclasses should use this to implement a function for getting a type-parameterized [Registration] subclass.
     * For example:
     * ```
     * operator fun <C : AstConstraint<C>> get(id: ConstraintId<C>): ConstraintSerdeRegistration<C> {
     *     return super._get(id) as ConstraintSerdeRegistration<C>
     * }
     * ```
     */
    protected fun <C : AstConstraint<C>> _get(id: ConstraintId<C>): Registration<C> {
        // Compiler thinks that this is an unchecked cast, but we know it's safe because the type bound of Registration
        // and ConstraintId must be the same, and we've already verified that the ID matches.
        return registrations.single { it.id == id } as Registration<C>
    }

    interface Registration<T : AstConstraint<T>> {
        val id: ConstraintId<T>
    }
}
