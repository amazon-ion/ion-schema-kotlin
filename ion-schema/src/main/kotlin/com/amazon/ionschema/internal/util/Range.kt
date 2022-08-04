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

import com.amazon.ion.IonInt
import com.amazon.ion.IonList
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException

/**
 * Enum representing the support types of ranges.
 */
internal enum class RangeType {
    INT,
    INT_NON_NEGATIVE,
    ION_NUMBER,
    ION_TIMESTAMP,
    ION_TIMESTAMP_PRECISION,
}

/**
 * Interface for all range implementations.
 */
internal interface Range<in T> {
    operator fun contains(value: T): Boolean
}

/**
 * Factory method for instantiating Range<T> objects.
 */
internal class RangeFactory {
    companion object {
        fun <T> rangeOf(ion: IonValue, rangeType: RangeType): Range<T> {
            if (ion.isNullValue) {
                throw InvalidSchemaException("Invalid range $ion")
            }

            val ionList = when (ion) {
                !is IonList -> {
                    val range = ion.system.newList(ion.clone(), ion.clone())
                    range.addTypeAnnotation("range")
                    range
                }
                else -> ion
            }

            checkRange(ionList)

            @Suppress("UNCHECKED_CAST")
            return when (rangeType) {
                RangeType.INT -> RangeInt(ionList)
                RangeType.INT_NON_NEGATIVE -> RangeIntNonNegative(ionList)
                RangeType.ION_NUMBER -> RangeIonNumber(ionList)
                RangeType.ION_TIMESTAMP -> RangeIonTimestamp(ionList)
                RangeType.ION_TIMESTAMP_PRECISION -> RangeIonTimestampPrecision(ionList)
            } as Range<T>
        }
    }
}

internal fun checkRange(ion: IonList) {
    when {
        !ion.hasTypeAnnotation("range") ->
            throw InvalidSchemaException("Invalid range, missing 'range' annotation:  $ion")
        ion.size != 2 ->
            throw InvalidSchemaException("Invalid range, size of list must be 2:  $ion")
        ion[0].isNullValue || ion[1].isNullValue || (isRangeMin(ion[0]) && isRangeMax(ion[1])) ->
            throw InvalidSchemaException("Invalid range $ion")
    }
}

internal fun isRangeMin(ion: IonValue) = (ion as? IonSymbol)?.stringValue().equals("min")
internal fun isRangeMax(ion: IonValue) = (ion as? IonSymbol)?.stringValue().equals("max")

internal fun toInt(ion: IonValue) = (ion as? IonInt)?.intValue()
