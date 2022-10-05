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

import com.amazon.ion.IonDatagram
import com.amazon.ion.IonList
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.CommonViolations
import com.amazon.ionschema.internal.Constraint
import com.amazon.ionschema.internal.util.IntRange
import com.amazon.ionschema.internal.util.withoutTypeAnnotations

/**
 * Implements the annotations constraint.
 *
 * Invocations are delegated to either an Ordered or Unordered implementation.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#annotations
 */
internal class Annotations_1_0 private constructor(
    ion: IonValue,
    private val delegate: Constraint
) : ConstraintBase(ion), Constraint by delegate {

    constructor(ion: IonValue) : this(ion, delegate(ion))

    companion object {
        private fun delegate(ion: IonValue): Constraint {
            val requiredByDefault = ion.hasTypeAnnotation("required")
            if (ion !is IonList || ion.isNullValue) {
                throw InvalidSchemaException("Expected annotations as a list, found: $ion")
            }
            val annotations = ion.map {
                Annotation(it as IonSymbol, requiredByDefault)
            }
            return if (ion.hasTypeAnnotation("ordered")) {
                OrderedAnnotations(ion, annotations)
            } else {
                UnorderedAnnotations(ion, annotations)
            }
        }
    }

    override val name = delegate.name
}

/**
 * Ordered implementation of the annotations constraint, backed by a [StateMachine].
 */
internal class OrderedAnnotations(
    ion: IonValue,
    private val annotations: List<Annotation>
) : ConstraintBase(ion) {

    private val ION = ion.system

    private val stateMachine: StateMachine

    init {
        val stateMachineBuilder = StateMachineBuilder().apply {
            if (!ion.hasTypeAnnotation("closed")) withOpenContent()
        }
        var state: State? = null
        (ion as IonList).forEachIndexed { idx, it ->
            val newState = State(
                occurs = when {
                    annotations[idx].isRequired -> IntRange.REQUIRED
                    else -> IntRange.OPTIONAL
                },
                isFinal = idx == ion.size - 1
            )
            val annotationSymbol = it.withoutTypeAnnotations()
            stateMachineBuilder.addTransition(state, EventIonValue(annotationSymbol), newState)

            state = newState
        }

        stateMachine = stateMachineBuilder.build()
    }

    override fun validate(value: IonValue, issues: Violations) {
        if (value is IonDatagram) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
            return
        }

        if (!stateMachine.matches(value.typeAnnotations.map { ION.newSymbol(it) }.iterator())) {
            issues.add(Violation(ion, "annotations_mismatch", "annotations don't match expectations"))
        }
    }
}

/**
 * Unordered implementation of the annotations constraint.
 */
internal class UnorderedAnnotations(
    ion: IonValue,
    private val annotations: List<Annotation>
) : ConstraintBase(ion) {

    private val closedAnnotationStrings: List<String>? = if (ion.hasTypeAnnotation("closed")) (ion as IonList).map { (it as IonSymbol).stringValue() } else null

    override fun validate(value: IonValue, issues: Violations) {
        if (value is IonDatagram) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
            return
        }

        val missingAnnotations = mutableListOf<Annotation>()
        annotations.forEach {
            if (it.isRequired && !value.hasTypeAnnotation(it.text)) {
                missingAnnotations.add(it)
            }
        }

        if (missingAnnotations.size > 0) {
            issues.add(
                Violation(
                    ion, "missing_annotation",
                    "missing annotation(s): " + missingAnnotations.joinToString { it.text }
                )
            )
        }

        closedAnnotationStrings?.let {
            if (!it.containsAll(value.typeAnnotations.toList())) {
                issues.add(Violation(ion, "unexpected_annotation", "found one or more unexpected annotations"))
            }
        }
    }
}

internal class Annotation(
    ion: IonSymbol,
    requiredByDefault: Boolean
) {
    val text = ion.stringValue()

    val isRequired = when {
        ion.hasTypeAnnotation("required") -> true
        ion.hasTypeAnnotation("optional") -> false
        else -> requiredByDefault
    }
}
