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

package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue
import software.amazon.ionschema.Violation
import software.amazon.ionschema.Violations
import software.amazon.ionschema.internal.util.RangeFactory
import software.amazon.ionschema.internal.util.RangeType

/**
 * Base class for constraints that validate an int value
 * against a non-negative int range.
 */
internal abstract class ConstraintBaseIntRange<T : IonValue>(
        private val expectedClass: Class<out IonValue>,
        ion: IonValue
) : ConstraintBase(ion) {

    internal val range = RangeFactory.rangeOf<Int>(ion, RangeType.INT_NON_NEGATIVE)

    internal abstract val violationCode: String
    internal abstract val violationMessage: String

    override fun validate(value: IonValue, issues: Violations) {
        validateAs(expectedClass, value, issues) { v ->
            @Suppress("UNCHECKED_CAST")
            val intValue = getIntValue(v as T)
            if (!range.contains(intValue)) {
                issues.add(Violation(ion, violationCode, violationMessage.format(intValue, range)))
            }
        }
    }

    abstract fun getIntValue(value: T): Int
}

