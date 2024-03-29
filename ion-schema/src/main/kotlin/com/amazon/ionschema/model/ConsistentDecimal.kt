package com.amazon.ionschema.model

import com.amazon.ion.IonNumber
import java.math.BigDecimal

/**
 * Wrapper for BigDecimal where [equals], [hashCode], and [compareTo] are all consistent.
 */
class ConsistentDecimal(val bigDecimalValue: BigDecimal) : Comparable<ConsistentDecimal> {

    private val normalized: BigDecimal = bigDecimalValue.stripTrailingZeros()

    override fun compareTo(other: ConsistentDecimal) = normalized.compareTo(other.normalized)

    override fun equals(other: Any?) = other is ConsistentDecimal && normalized == other.normalized

    override fun hashCode() = normalized.hashCode()

    override fun toString(): String {
        return "ConsistentDecimal(${normalized.unscaledValue()}E${normalized.scale() * -1})"
    }

    companion object {
        /**
         * Constructs a new [ConsistentDecimal] with the value from the given [IonNumber].
         */
        @JvmStatic
        fun fromIonNumber(ionNumber: IonNumber) = ConsistentDecimal(ionNumber.bigDecimalValue())

        /**
         * Translates a long value into a [ConsistentDecimal] with a scale of zero.
         */
        @JvmStatic
        fun valueOf(long: Long) = ConsistentDecimal(BigDecimal.valueOf(long))

        /**
         * Translates a [Double] into a [ConsistentDecimal], using the [Double]'s canonical string representation
         * provided by the [Double.toString] method.
         */
        @JvmStatic
        fun valueOf(double: Double) = ConsistentDecimal(BigDecimal.valueOf(double))
    }
}
