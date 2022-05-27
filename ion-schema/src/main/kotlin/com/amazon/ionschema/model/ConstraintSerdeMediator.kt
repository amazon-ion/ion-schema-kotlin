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

/**
 * Why is there a [ConstraintSerdeMediator] and a [ConstraintValidatorMediator] instead of one big mediator?
 * In theory, the AST should be language-version agnostic, so we want to be able to have 1 [ConstraintValidatorMediator],
 * and many [ConstraintSerdeMediator]s.
 */
class ConstraintSerdeMediator(registrations: List<ConstraintSerdeRegistration<*>>) : ConstraintMediator<ConstraintSerdeRegistration<*>>(registrations) {
    operator fun <C : AstConstraint<C>> get(id: ConstraintId<C>): ConstraintSerdeRegistration<C> {
        return super._get(id) as ConstraintSerdeRegistration<C>
    }

    /**
     * Writes a single [AstConstraint] to a [StructField]
     */
    inline fun <reified T : AstConstraint<T>> writeConstraint(constraint: AstConstraint<T>): StructField {
        return field(constraint.id.constraintName, this[constraint.id].write(constraint as T))
    }

    /**
     * Reads a single [StructField] to produce an [AstConstraint]
     */
    fun readConstraint(field: StructField): AstConstraint<*> {
        return get(field.name).read(field.value)
    }
}

class ConstraintSerdeRegistration<T : AstConstraint<T>>(
    override val id: ConstraintId<T>,
    val read: (AnyElement) -> T,
    val write: (T) -> IonElement
) : ConstraintMediator.Registration<T>

// Example of how the ConstraintMediator can be used to read ISL and construct the AST.
fun readType(ion: StructElement, serdeMediator: ConstraintSerdeMediator): AstType.TypeDefinition {
    return AstType.TypeDefinition(
        constraints = ion.fields
            .filter { it.name in serdeMediator } // Find the fields that are constraints
            .map { serdeMediator.readConstraint(it) } // Read the fields as AstConstraint
            .toSet(),
        userContent = ion.fields
            .filterNot { it.name in serdeMediator && it.name == "name" }
            .map { it.toPair() }
    )
}

// Example of how the ConstraintMediator can be used to write ISL from the AST.
fun writeType(type: AstType.TypeDefinition, serdeMediator: ConstraintSerdeMediator): IonElement {
    val fields: Iterable<StructField> = type.constraints.map { serdeMediator.writeConstraint(it) } + type.userContent.map { it.toStructField() }
    return ionStructOf(fields)
}

// Utility functions for working with StructFields
fun Pair<String, IonElement>.toStructField() = field(first, second)
fun StructField.toPair() = name to value
