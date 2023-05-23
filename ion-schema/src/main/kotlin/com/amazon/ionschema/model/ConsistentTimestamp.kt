package com.amazon.ionschema.model

import com.amazon.ion.IonTimestamp
import com.amazon.ion.Timestamp
import java.math.BigDecimal

/**
 * Wrapper for Timestamp where [equals], [hashCode], and [compareTo] are all consistent.
 */
class ConsistentTimestamp(val timestampValue: Timestamp) : Comparable<ConsistentTimestamp> {

    private val normalizedMillis: BigDecimal = timestampValue.decimalMillis.stripTrailingZeros()

    override fun compareTo(other: ConsistentTimestamp) = normalizedMillis.compareTo(other.normalizedMillis)

    override fun equals(other: Any?) = other is ConsistentTimestamp && normalizedMillis == other.normalizedMillis

    override fun hashCode() = normalizedMillis.hashCode()

    override fun toString(): String {
        return "ConsistentTimestamp($timestampValue)"
    }

    companion object {
        /**
         * Constructs a new [ConsistentTimestamp] with the value from the given [IonTimestamp].
         */
        @JvmStatic
        fun fromIonTimestamp(ionTimestamp: IonTimestamp) = ConsistentTimestamp(ionTimestamp.timestampValue())
    }
}
