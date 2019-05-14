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

package software.amazon.ionschema.internal.util

import software.amazon.ion.IonList
import software.amazon.ion.IonTimestamp
import software.amazon.ionschema.InvalidSchemaException

/**
 * Implementation of Range<IonTimestamp> which mostly delegates to RangeBigDecimal.
 */
internal class RangeIonTimestamp private constructor (
        private val delegate: RangeBigDecimal
) : Range<IonTimestamp> {

    constructor (ion: IonList) : this(toRangeBigDecimal(ion))

    companion object {
        private fun toRangeBigDecimal(ion: IonList): RangeBigDecimal {
            checkRange(ion)

            // convert to a decimal range
            val newRange = ion.system.newEmptyList()
            newRange.addTypeAnnotation("range")
            ion.forEach { ionValue ->
                val newValue = if (ionValue is IonTimestamp) {
                    if (ionValue.localOffset == null) {
                        throw InvalidSchemaException(
                                "Timestamp range bound doesn't specify a local offset: $ionValue")
                    }
                    ion.system.newDecimal(ionValue.decimalMillis)
                } else {
                    ionValue.clone()
                }
                ionValue.typeAnnotations.forEach { newValue.addTypeAnnotation(it) }
                newRange.add(newValue)
            }

            return RangeBigDecimal(newRange)
        }
    }

    override fun contains(value: IonTimestamp): Boolean {
        // ValidValues performs this same check and adds a Violation
        // instead of invoking this method;  this if is here purely
        // as a defensive safety check, and will ideally never be true
        if (value.localOffset == null) {
            throw IllegalArgumentException("Unable to compare timestamp with unknown local offset: $value")
        }
        return delegate.contains(value.decimalMillis)
    }
}

