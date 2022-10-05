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

import com.amazon.ion.IonContainer
import com.amazon.ion.IonList
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations

/**
 * Implements the contains constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#contains
 */
internal class Contains(
    ion: IonValue
) : ConstraintBase(ion) {

    private val expectedElements = if (ion !is IonList || ion.isNullValue) {
        throw InvalidSchemaException("Expected values in a list, found: $ion")
    } else if (ion.typeAnnotations.isNotEmpty()) {
        throw InvalidSchemaException("List of values may not be annotated.")
    } else {
        ion.toArray()
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonContainer>(value, issues) { v ->
            val expectedValues = expectedElements.toMutableSet()
            v.forEach {
                expectedValues.remove(it)
            }
            if (!expectedValues.isEmpty()) {
                issues.add(
                    Violation(
                        ion, "missing_values",
                        "missing value(s): " + expectedValues.joinToString { it.toString() }
                    )
                )
            }
        }
    }
}
