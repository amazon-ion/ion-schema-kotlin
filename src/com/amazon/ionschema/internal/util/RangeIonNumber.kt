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

package com.amazon.ionschema.internal.util

import com.amazon.ion.*
import java.math.BigDecimal

/**
 * Implementation of Range<IonValue> restricted to IonDecimal, IonFloat,
 * and IonInt (numeric) values.  Mostly delegates to RangeBigDecimal.
 */
internal class RangeIonNumber private constructor (
        private val delegate: RangeBigDecimal
) : Range<IonValue> {

    constructor (ion: IonList) : this(RangeBigDecimal(ion))

    companion object {
        private fun toBigDecimal(ion: IonValue) =
                if (ion.isNullValue) {
                    null
                } else {
                    when (ion) {
                        is IonDecimal -> ion.bigDecimalValue()
                        is IonFloat -> if (ion.isNumericValue) {
                                ion.bigDecimalValue()
                            } else {
                                null
                            }
                        is IonInt -> BigDecimal(ion.bigIntegerValue())
                        else -> null
                    }
                }
    }

    override fun contains(value: IonValue): Boolean {
        val bdValue = toBigDecimal(value)
        return if (bdValue != null) {
                delegate.contains(bdValue)
            } else {
                false
            }
    }
}

