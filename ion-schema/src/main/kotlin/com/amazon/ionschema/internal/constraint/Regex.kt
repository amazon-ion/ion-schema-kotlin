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

import com.amazon.ion.IonString
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.validateRegexPattern
import java.util.regex.Pattern

/**
 * Implements the regex constraint.  This implementation translates
 * the regex features defined by the Ion Schema Specification to
 * Java's Pattern class, and makes a best-effort to error if the
 * caller tries to use a feature of Pattern that is NOT defined
 * in the Ion Schema Specification.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-1-0/spec#regex
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#regex
 * @see java.util.regex.Pattern
 */
internal class Regex(
    ion: IonValue,
    private val islVersion: IonSchemaVersion
) : ConstraintBase(ion) {

    private val pattern: Pattern

    init {
        islRequire(ion is IonString && !ion.isNullValue && ion.stringValue().isNotEmpty()) {
            "Regex must be a non-empty string; but was: $ion"
        }

        var flags = 0
        ion.typeAnnotations.forEach {
            val flag = when (it) {
                "i" -> Pattern.CASE_INSENSITIVE
                "m" -> Pattern.MULTILINE
                else -> throw InvalidSchemaException(
                    "Unrecognized flags for regex ($ion)"
                )
            }
            flags = flags.or(flag)
        }
        val patternString = validateRegexPattern(ion.stringValue(), islVersion)
        pattern = Pattern.compile(patternString, flags)
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonText>(value, issues) { v ->
            if (!pattern.matcher(v.stringValue()).find()) {
                issues.add(
                    Violation(
                        ion, "regex_mismatch",
                        "'${v.stringValue()}' doesn't match regex '${pattern.pattern()}'"
                    )
                )
            }
        }
    }
}
