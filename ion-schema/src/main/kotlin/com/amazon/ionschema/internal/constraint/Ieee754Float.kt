/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.ion.IonFloat
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.Violation
import com.amazon.ionschema.Violations
import com.amazon.ionschema.internal.constraint.Ieee754Float.Ieee754InterchangeFormat.binary16
import com.amazon.ionschema.internal.constraint.Ieee754Float.Ieee754InterchangeFormat.binary32
import com.amazon.ionschema.internal.constraint.Ieee754Float.Ieee754InterchangeFormat.binary64
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * Implements the ieee754_float constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/isl-2-0/spec#ieee754_float
 */
internal class Ieee754Float(ion: IonValue) : ConstraintBase(ion) {

    /**
     * Represents a range of floating point numbers and the interval between the representable values in that range.
     */
    private data class PrecisionInfo(val range: ClosedRange<Double>, val interval: Double)

    companion object {
        /**
         * A list of [PrecisionInfo] that models the precision limitations of a 16-bit float. The available precision
         * can be calculated to produce a table, such as the table for 16-bit floats found
         * [here](https://en.wikipedia.org/wiki/Half-precision_floating-point_format#Precision_limitations).
         *
         * Rather than creating a generalized solution or converting to and from 16-bit floats, we have, essentially,
         * hardcoded some values. This approach is justifiable because 16-bit floats are small enough that we can do
         * it, this is the only floating point format for which it is needed, and it avoids the complexity of a
         * generalized solution.
         *
         * See https://github.com/amzn/ion-schema-kotlin/issues/218.
         *
         * Technically, there is overlap between the ranges, but that's okay because the overlap is only the boundary
         * value, which will be valid regardless of which interval we test it against.
         */
        private val BINARY_16_PRECISION_RANGES = sequence {
            yield(PrecisionInfo(0.0..2.0.pow(-14), interval = 2.0.pow(-24))) // Subnormal numbers
            yieldAll(
                (-14..14).asSequence().map {
                    PrecisionInfo(2.0.pow(it)..2.0.pow(it + 1), interval = 2.0.pow(it - 10))
                }
            )
            // 65_504 is the largest precisely representable number
            yield(PrecisionInfo(2.0.pow(15)..65_504.0, interval = 2.0.pow(5)))
        }.toList()
    }

    /**
     * The possible values for the `ieee754_float` constraint.
     */
    private enum class Ieee754InterchangeFormat {
        binary16,
        binary32,
        binary64;
        companion object {
            val names = Ieee754InterchangeFormat.values().map { it.name }
        }
    }

    private val interchangeFormat = ion.let {
        islRequireIonTypeNotNull<IonSymbol>(it) { "ieee754_float must be a non-null Ion symbol" }
        islRequire(it.typeAnnotations.isEmpty()) { "ieee754_float must not have annotations" }
        islRequire(it.stringValue() in Ieee754InterchangeFormat.names) { "ieee754_float must be one of ${Ieee754InterchangeFormat.names}" }
        return@let Ieee754InterchangeFormat.valueOf(it.stringValue())
    }

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonFloat>(value, issues) {
            if (!(interchangeFormat losslesslyEncodes it)) {
                issues.add(Violation(ion, "invalid_ieee754_float", "value cannot be losslessly represented by the IEEE-754 $interchangeFormat interchange format."))
            }
        }
    }

    private infix fun Ieee754InterchangeFormat.losslesslyEncodes(value: IonFloat): Boolean {
        // +inf, -inf, and nan are always valid
        if (!value.doubleValue().isFinite()) return true

        return when (this) {
            binary16 -> value.doubleValue().isExactHalfValue()
            binary32 -> value.doubleValue().toFloat().toDouble() == value.doubleValue()
            binary64 -> true // All Ion Floats are 64 bits or smaller.
        }
    }

    private fun Double.isExactHalfValue(): Boolean {
        if (this.absoluteValue > 65504) return false
        // Find the appropriate interval for the Double value we are testing
        val (_, interval) = BINARY_16_PRECISION_RANGES.first { (range, _) -> absoluteValue in range }
        // If the value is an exact multiple of the interval between two Half values, then we know it
        // is possible to _exactly_ represent the value as a Half precision float.
        return this % interval == 0.0
    }
}
