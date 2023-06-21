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

package com.amazon.ionschema.internal.constraint

import com.amazon.ion.IonList
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Type
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.DeferredReferenceManager
import com.amazon.ionschema.internal.SchemaInternal
import com.amazon.ionschema.internal.TypeInternal
import com.amazon.ionschema.internal.TypeReference

/**
 * Base class for logic constraint implementations.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/spec.html#logic-constraints
 */
internal abstract class LogicConstraints(
    ion: IonValue,
    schema: SchemaInternal,
    referenceManager: DeferredReferenceManager,
) : ConstraintBase(ion) {

    internal val types = if (ion is IonList && !ion.isNullValue) {
        ion.map { TypeReference.create(it, schema, referenceManager) }
    } else {
        throw InvalidSchemaException("Expected a list, found: $ion")
    }

    internal fun validateTypes(value: IonValue, issues: Violations): List<Type> {
        val validTypes = mutableListOf<Type>()
        types.forEach {
            val checkpoint = issues.checkpoint()
            it().validate(value, issues)
            if (checkpoint.isValid()) {
                validTypes.add(it())
            }
        }
        return validTypes
    }
}

/**
 * Implements the all_of constraint.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/spec.html#all_of
 */
internal class AllOf(ion: IonValue, schema: SchemaInternal, referenceManager: DeferredReferenceManager) : LogicConstraints(ion, schema, referenceManager) {
    override fun validate(value: IonValue, issues: Violations) {
        val allOfViolation = Violation(ion, "all_types_not_matched")
        val count = validateTypes(value, allOfViolation).size
        if (count != types.size) {
            allOfViolation.message = "value matches $count types, expected ${types.size}"
            issues.add(allOfViolation)
        }
    }
}

/**
 * Implements the any_of constraint.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/spec.html#any_of
 */
internal class AnyOf(ion: IonValue, schema: SchemaInternal, referenceManager: DeferredReferenceManager) : LogicConstraints(ion, schema, referenceManager) {
    override fun validate(value: IonValue, issues: Violations) {
        val anyOfViolation = Violation(ion, "no_types_matched", "value matches none of the types")
        types.forEach {
            val checkpoint = anyOfViolation.checkpoint()
            it().validate(value, anyOfViolation)
            // We can exit at the first valid type we encounter
            if (checkpoint.isValid()) return
        }
        issues.add(anyOfViolation)
    }
}

/**
 * Implements the one_of constraint.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/spec.html#one_of
 */
internal class OneOf(ion: IonValue, schema: SchemaInternal, referenceManager: DeferredReferenceManager) : LogicConstraints(ion, schema, referenceManager) {
    override fun validate(value: IonValue, issues: Violations) {
        val oneOfViolation = Violation(ion)
        val validTypes = validateTypes(value, oneOfViolation)
        if (validTypes.size != 1) {
            if (validTypes.size == 0) {
                oneOfViolation.code = "no_types_matched"
                oneOfViolation.message = "value matches none of the types"
            }
            if (validTypes.size > 1) {
                oneOfViolation.code = "more_than_one_type_matched"
                oneOfViolation.message = "value matches %s types, expected 1".format(validTypes.size)

                validTypes.forEach {
                    val typeDef = (it as TypeInternal).isl
                    oneOfViolation.add(
                        Violation(
                            typeDef, "type_matched",
                            "value matches type %s".format(typeDef)
                        )
                    )
                }
            }
            issues.add(oneOfViolation)
        }
    }
}

/**
 * Implements the not constraint.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/spec.html#not
 */
internal class Not(ion: IonValue, schema: SchemaInternal, referenceManager: DeferredReferenceManager) : ConstraintBase(ion) {
    private val type = TypeReference.create(ion, schema, referenceManager)

    override fun validate(value: IonValue, issues: Violations) {
        val child = Violation(ion, "type_matched", "value unexpectedly matches type")
        type().validate(value, child)
        if (child.isValid()) {
            issues.add(child)
        }
    }
}
