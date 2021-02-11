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
import com.amazon.ionschema.internal.util.Range
import com.amazon.ionschema.Violations
import com.amazon.ionschema.Violation
import com.amazon.ionschema.internal.util.RangeFactory
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

    private val validRange = buildRange(ion)

    private val validValues =
            if (validRange == null && ion is IonList && !ion.isNullValue) {
                ion.filter { checkValue(it) }.toSet()
            } else {
                null
            }

    private fun isValidRange(ion: IonValue) = ion is IonList && !ion.isNullValue && ion.hasTypeAnnotation("range")

    private val anyRangeValues = validValues?.any{ isValidRange(it) } ?: false

    init {
        if (validRange == null && validValues == null) {
            throw InvalidSchemaException("Invalid valid_values constraint: $ion")
        }
    }

    // build range value from given ion value
    private fun buildRange(ion: IonValue) =
        if (ion is IonList && isValidRange(ion)) {
            if (ion[0] is IonTimestamp || ion[1] is IonTimestamp) {
                @Suppress("UNCHECKED_CAST")
                RangeFactory.rangeOf<IonTimestamp>(ion, RangeType.ION_TIMESTAMP) as Range<IonValue>
            } else {
                RangeFactory.rangeOf<IonValue>(ion, RangeType.ION_NUMBER)
            }
        } else {
            null
        }

    private fun checkValue(ion: IonValue) =
        if (isValidRange(ion)) {
            true
        } else if (ion.typeAnnotations.isNotEmpty()) {
            throw InvalidSchemaException("Annotations ($ion) are not allowed in valid_values")
        } else {
            true
        }

    override fun validate(value: IonValue, issues: Violations) {
        if (validRange != null) {
            if (value is IonTimestamp && value.localOffset == null) {
                issues.add(Violation(ion, "unknown_local_offset",
                        "unable to compare timestamp with unknown local offset"))
                return
            }
            if (!validRange.contains(value)) {
                issues.add(Violation(ion, "invalid_value", "invalid value $value"))
            }
        } else {
            val v = value.withoutTypeAnnotations()
            // check any range exists in valid_values list and validate
            if (anyRangeValues) {
                if (!validValues!!.any {possibility ->
                            if (isValidRange(possibility)) {
                                buildRange(possibility)!!.contains(v)
                            } else {
                                possibility == v
                            }
                        }) {
                    issues.add(Violation(ion, "invalid_value", "invalid value $v"))
                }
            } else {
                if (!validValues!!.contains(v)) {
                    issues.add(Violation(ion, "invalid_value", "invalid value $v"))
                }
            }
        }
    }
}

