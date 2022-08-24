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
import com.amazon.ion.IonTimestamp
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.util.Range
import com.amazon.ionschema.internal.util.RangeFactory
import com.amazon.ionschema.internal.util.RangeIonNumber
import com.amazon.ionschema.internal.util.RangeIonTimestamp
import com.amazon.ionschema.internal.util.RangeType
import com.amazon.ionschema.internal.util.withoutTypeAnnotations

/**
 * Implements the valid_values constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#valid_values
 */
internal class ValidValues(
    ion: IonValue
) : ConstraintBase(ion) {

    // store either the ranges that are built or the ion value to be used for validation
    private val validValues = (
        if (isValidRange(ion)) {
            // convert range::[x,y] to [range::[x,y]] for simplicity in verifying and storing valid_values
            ion.system.newList(ion.clone())
        } else if (ion is IonList && !ion.isNullValue) {
            ion.onEach { checkValue(it) }.toSet()
        } else {
            null
        }
        )?.map { buildRange(it) }

    private fun isValidRange(ion: IonValue) = ion is IonList && !ion.isNullValue && ion.hasTypeAnnotation("range")

    init {
        if (validValues == null) {
            throw InvalidSchemaException("Invalid valid_values constraint: $ion")
        }
    }

    // build range value from given ion value if valid range or return ion value itself
    private fun buildRange(ion: IonValue) =
        if (ion is IonList && isValidRange(ion)) {
            if (ion[0] is IonTimestamp || ion[1] is IonTimestamp) {
                @Suppress("UNCHECKED_CAST")
                RangeFactory.rangeOf<IonTimestamp>(ion, RangeType.ION_TIMESTAMP) as Range<IonValue>
            } else {
                RangeFactory.rangeOf<IonValue>(ion, RangeType.ION_NUMBER)
            }
        } else {
            ion
        }

    private fun checkValue(ion: IonValue) =
        if (isValidRange(ion)) {
            ion
        } else if (ion.typeAnnotations.isNotEmpty()) {
            throw InvalidSchemaException("Annotations ($ion) are not allowed in valid_values")
        } else {
            ion
        }

    override fun validate(value: IonValue, issues: Violations) {
        // FYI--a document/IonDatagram is never valid for the valid_values constraint.
        // However, we don't need to test for it because there's no way to construct
        // a valid_values that could match an IonDatagram.

        val v = value.withoutTypeAnnotations()
        val matchesAny = validValues!!.any { possibility ->
            when (possibility) {
                is IonValue -> v == possibility
                is RangeIonNumber -> v in possibility
                is RangeIonTimestamp -> when {
                    v !is IonTimestamp -> false
                    else -> v in possibility
                }
                else -> TODO("This is unreachable.")
            }
        }

        if (!matchesAny) {
            issues.add(Violation(ion, "invalid_value", "invalid value $v"))
        }
    }
}
