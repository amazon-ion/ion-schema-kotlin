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
import com.amazon.ionschema.IonSchemaVersion.v2_0
import com.amazon.ionschema.Schema
import com.amazon.ionschema.Violation
import com.amazon.ionschema.ViolationChild
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.TypeReference
import com.amazon.ionschema.internal.util.IntRange
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.schemaTypeName

/**
 * Implements the ordered_element constraint.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#ordered_elements
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#ordered_elements
 */
internal class OrderedElements(
    ion: IonValue,
    private val schema: Schema
) : ConstraintBase(ion) {

    private val nfa: NFA<IonValue, Violation> = run {
        islRequireIonTypeNotNull<IonList>(ion) { "Invalid ordered_elements constraint: $ion" }
        if (schema.ionSchemaLanguageVersion >= v2_0) {
            islRequire(ion.typeAnnotations.isEmpty()) { "ordered_elements list may not be annotated. Found: $ion" }
        }

        val stateBuilder = OrderedElementsNfaStatesBuilder()
        ion.forEachIndexed { idx, it ->
            val occursRange = IntRange.toIntRange((it as? IonStruct)?.get("occurs")) ?: IntRange.REQUIRED
            val typeRef = TypeReference.create(it, schema, variablyOccurring = true)
            stateBuilder.addState(
                min = occursRange.lower,
                max = occursRange.upper,
                matches = { Violation().let { v -> typeRef().validate(it, v); NFA.State.Decision(v.isValid(), v.singleOrNull()) } },
                description = "<ELEMENT $idx> $it",
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
            val outcome = nfa.matches(v, debug)

            if (outcome is NFA.Outcome.IsNotMatch<*>) {
                issues.add(
                    Violation(
                        ion, "ordered_elements_mismatch",
                        "one or more ordered elements don't match specification"
                    ).apply { add(buildViolationForValue(v, outcome.eventId, outcome.reasons)) }
                )
            }
        }
    }

    /**
     * Builds a violation that describes the element of the sequence where it was discovered that the data was invalid.
     * Note that this is not necessarily the "reason" the data is invalid. For example, if you expect letters in
     * alphabetical order, then (a c f r n p t) is invalid. The _fact_ that it is invalid will be detected at the letter
     * 'n', but it's debatable whether 'n' is the problem or if the problem is the the 'r' that precedes it. That
     * distinction is undecidable unless we can assign meaning to incorrect data, which is outside the scope of Ion Schema.
     */
    private fun buildViolationForValue(value: IonSequence, idx: Int, reasons: Set<NFA.InvalidTransition<out Any>>): ViolationChild {

        val isEndOfSequenceEvent = idx == value.size
        val valueViolation = ViolationChild(
            index = if (isEndOfSequenceEvent) null else idx,
            fieldName = if (isEndOfSequenceEvent) "<END OF ${value.type.schemaTypeName().toUpperCase()}>" else null,
            value = value.getOrNull(idx)
        )

        val describe: NFA.State<*, *>.() -> String = {
            when (this) {
                NFA.State.Final -> "<END OF ${value.type.schemaTypeName().toUpperCase()}>"
                else -> description
            }
        }

        reasons.forEach {
            val message = when (it) {
                is NFA.InvalidTransition.CannotEnterState<*, *> -> "does not match: ${it.toState.describe()}"
                is NFA.InvalidTransition.CannotExitState<*, *> -> "min occurs not reached: ${it.fromState.describe()}"
                is NFA.InvalidTransition.CannotReenterState<*, *> -> "max occurs already reached: ${it.state.describe()}"
            }
            valueViolation.add(
                Violation(message = message).apply {
                    if (it is NFA.InvalidTransition.CannotEnterState<*, *> && it.reason is Violations) {
                        it.reason.violations.forEach { this.add(it) }
                        it.reason.children.forEach { this.add(it) }
                    }
                }
            )
        }

        return valueViolation
    }
}
