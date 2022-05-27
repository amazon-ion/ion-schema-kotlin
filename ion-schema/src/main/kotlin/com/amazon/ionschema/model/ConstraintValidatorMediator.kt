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

import com.amazon.ionelement.api.AnyElement
import com.amazon.ionelement.api.IonElement
import com.amazon.ionschema.model.constraints.ByteLengthConstraint
import com.amazon.ionschema.model.constraints.ContainsConstraint

class ConstraintValidatorMediator(registrations: List<ConstraintValidatorRegistration<*>>) : ConstraintMediator<ConstraintValidatorRegistration<*>>(registrations) {
    private operator fun <C : AstConstraint<C>> get(id: ConstraintId<C>): ConstraintValidatorRegistration<C> {
        return super._get(id) as ConstraintValidatorRegistration<C>
    }

    /**
     * Validates an IonElement against a single constraint
     */
    inline fun <reified T : AstConstraint<T>> validateConstraint(constraint: AstConstraint<T>, data: IonElement): Boolean {
        return validateConstraint(constraint as T, data.asAnyElement())
    }

    fun <T : AstConstraint<T>> validateConstraint(constraint: T, data: AnyElement): Boolean =
        this[constraint.id].validate(constraint, data)
}

class ConstraintValidatorRegistration<T : AstConstraint<T>>(
    override val id: ConstraintId<T>,
    val validate: (T, AnyElement) -> Boolean
) : ConstraintMediator.Registration<T>

infix fun <T : AstConstraint<T>> ConstraintId<T>.uses(validate: (T, AnyElement) -> Boolean) =
    ConstraintValidatorRegistration(this, validate)

val validators = ConstraintValidatorMediator(
    listOf(
        ByteLengthConstraint uses { constraint, data -> data.bytesValue.size() in constraint.range },
        ContainsConstraint uses { constraint, data -> data.containerValues.containsAll(constraint.values) }
    )
)

// Example of how the ConstraintMediator can be used to validate data against an AST type.
fun validateType(type: AstType.TypeDefinition, data: IonElement, mediator: ConstraintValidatorMediator): Boolean {
    // Note that for simplicity, this example uses a boolean return instead of collecting violations.
    return type.constraints.all { mediator.validateConstraint(it, data.asAnyElement()) }
}
