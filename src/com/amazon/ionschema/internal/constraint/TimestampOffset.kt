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
import com.amazon.ion.IonString
import com.amazon.ion.IonTimestamp
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations

/**
 * Implements the timestamp_offset constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#timestamp_offset
 */
internal class TimestampOffset(
        ion: IonValue
) : ConstraintBase(ion) {

    companion object {
        private val VALID_HOUR_RANGE = IntRange(0, 23)
        private val VALID_MINUTE_RANGE = IntRange(0, 59)
    }

    /**
     * This set contains the valid timestamp offsets translated to +/- minutes from UTC.
     * An unknown local offset is represented as null.  This approach corresponds
     * exactly with values returned by IonTimestamp.localOffset.
     */
    private val offsets: Set<Int?>
    private val offsetsAreValid: Boolean

    init {
        if (ion !is IonList) {
            throw InvalidSchemaException("timestamp_offset must be a list, was $ion")
        }
        if (ion.isNullValue) {
            throw InvalidSchemaException("timestamp_offset must not be a null value, was $ion")
        }
        if (ion.size == 0) {
            throw InvalidSchemaException("timestamp_offset must contain at least one offset")
        }

        offsetsAreValid = if (ion.typeAnnotations.isEmpty()) {
            false
        } else {
            if (ion.typeAnnotations.size != 1) {
                throw InvalidSchemaException("timestamp_offset allows for only 1 annotation, found ${ion.typeAnnotations}")
            }
            if (ion.typeAnnotations[0] != "not") {
                throw InvalidSchemaException("timestamp_offset can only be annotated with the token \"not\", found ${ion.typeAnnotations} ")
            }
            true
        }

        offsets = ion.map {
            // every timestamp offset must be of the form "[+|-]hh:mm"

            if (it !is IonString) {
                throw InvalidSchemaException("timestamp_offset values must be strings, found $it")
            }
            val str = it.stringValue()
            if (str == "-00:00") {
                null
            } else {
                if (str.length != 6 || str[3] != ':') {
                    throw InvalidSchemaException("timestamp_offset values must be of the form \"[+|-]hh:mm\"")
                }
                try {
                    val sign = when (str[0]) {
                        '-' -> -1
                        '+' ->  1
                        else -> throw InvalidSchemaException("Unrecognized timestamp offset sign '${str[0]}'")
                    }
                    // translate to offset in +/- minutes:
                    val hours = toInt(str.substring(1, 3), VALID_HOUR_RANGE)
                    val minutes = toInt(str.substring(4, 6), VALID_MINUTE_RANGE)
                    sign * (hours * 60 + minutes)
                } catch (e: NumberFormatException) {
                    throw InvalidSchemaException("Invalid timestamp offset '$str'")
                }
            }
        }.toSet()
    }

    private fun toInt(s: String, intRange: IntRange): Int {
        val int = s.toInt()
        if (!intRange.contains(int)) {
            throw NumberFormatException()
        }
        return int
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonTimestamp>(value, issues) { v ->
            val hasViolations =
                    if (offsetsAreValid) {
                        offsets.contains(v.localOffset)
                    } else {
                        !offsets.contains(v.localOffset)
                    }

            if (hasViolations) {
                issues.add(Violation(ion, "invalid_timestamp_offset",
                        "invalid timestamp offset %s, expected %s".format(v.localOffset, ion)))
            }
        }
    }
}

