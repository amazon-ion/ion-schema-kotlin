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
import com.amazon.ionschema.InvalidSchemaException

/**
 * Implements the precision constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#precision
 */
internal class Precision(
    ion: IonValue
) : ConstraintBaseIntRange<IonDecimal>(IonDecimal::class.java, ion) {

    init {
        if (range.contains(0)) {
            throw InvalidSchemaException("Precision must be at least 1 ($ion)")
        }
    }

    override val violationCode = "invalid_precision"
    override val violationMessage = "invalid precision %s, expected %s"

    override fun getIntValue(value: IonDecimal) = value.bigDecimalValue().precision()
}
