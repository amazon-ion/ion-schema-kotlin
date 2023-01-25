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

import com.amazon.ion.IonDecimal
import com.amazon.ion.IonValue
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.util.RangeFactory
import com.amazon.ionschema.internal.util.RangeType

/**
 * Implements the exponent constraint.
 *
 * @see https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#exponent
 */
internal class Exponent(
    ion: IonValue
) : ConstraintBase(ion) {

    internal val range = RangeFactory.rangeOf<Int>(ion, RangeType.INT)

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonDecimal>(value, issues) { v ->
            @Suppress("UNCHECKED_CAST")
            val exponent = v.bigDecimalValue().scale() * -1
            if (!range.contains(exponent)) {
                issues.add(Violation(ion, "invalid_exponent", "invalid exponent $exponent, expected $range"))
            }
        }
    }
}
