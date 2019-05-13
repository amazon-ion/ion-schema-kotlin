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

import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException

/**
 * Enum indicating whether an upper or lower range boundary is
 * inclusive or exclusive.
 */
internal enum class RangeBoundaryType {
    EXCLUSIVE,
    INCLUSIVE;

    companion object {
        fun forIon(ion: IonValue): RangeBoundaryType {
            val isExclusive = ion.hasTypeAnnotation("exclusive")
            return when {
                isRangeMin(ion) || isRangeMax(ion) -> {
                    if (isExclusive) {
                        throw InvalidSchemaException("Invalid range bound '$ion'")
                    }
                    INCLUSIVE
                }
                isExclusive -> EXCLUSIVE
                else -> INCLUSIVE
            }
        }
    }
}

