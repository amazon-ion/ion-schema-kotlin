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
import com.amazon.ion.IonSequence
import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.TypeReference
import com.amazon.ionschema.internal.util.IntRange

/**
 * Implements the ordered_element constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#ordered_elements
 */
internal class OrderedElements(
    ion: IonValue,
    private val schema: Schema
) : ConstraintBase(ion) {

    private val nfa: NFA<IonValue> = run {
        if (ion !is IonList || ion.isNullValue) {
            throw InvalidSchemaException("Invalid ordered_elements constraint: $ion")
        }

        val stateBuilder = OrderedElementsNfaStatesBuilder()
        ion.forEach {
            val occursRange = IntRange.toIntRange((it as? IonStruct)?.get("occurs")) ?: IntRange.REQUIRED
            val typeRef = TypeReference.create(it, schema)
            stateBuilder.addState(
                min = occursRange.lower,
                max = occursRange.upper,
                matches = { typeRef().isValid(it) }
            )
        }
        NFA(stateBuilder.build())
    }

    override fun validate(value: IonValue, issues: Violations) {
        validate(value, issues, debug = false)
    }

    // Visible for testing
    internal fun validate(value: IonValue, issues: Violations, debug: Boolean) {
        validateAs<IonSequence>(value, issues) { v ->
            if (!nfa.matches(v, debug)) {
                issues.add(
                    Violation(
                        ion, "ordered_elements_mismatch",
                        "one or more ordered elements don't match specification"
                    )
                )
            }
        }
    }
}
