package com.amazon.ionschema.model

import com.amazon.ion.Timestamp
import java.lang.Integer.max

class TimestampPrecisionValue private constructor(internal val intValue: Int) : Comparable<TimestampPrecisionValue> {

    override fun compareTo(other: TimestampPrecisionValue): Int = this.intValue.compareTo(other.intValue)
    override fun equals(other: Any?): Boolean = other is TimestampPrecisionValue && intValue == other.intValue
    override fun hashCode(): Int = intValue.hashCode()
    override fun toString(): String = "TimestampPrecisionValue($intValue)"

    companion object {
        @JvmStatic val Year = TimestampPrecisionValue(-4)
        @JvmStatic val Month = TimestampPrecisionValue(-3)
        @JvmStatic val Day = TimestampPrecisionValue(-2)
        @JvmStatic val Minute = TimestampPrecisionValue(-1)
        @JvmStatic val Second = TimestampPrecisionValue(0)
        @JvmStatic val Millisecond = TimestampPrecisionValue(3)
        @JvmStatic val Microsecond = TimestampPrecisionValue(6)
        @JvmStatic val Nanosecond = TimestampPrecisionValue(9)

        @JvmStatic
        fun values() = listOf(Year, Month, Day, Minute, Second, Millisecond, Microsecond, Nanosecond)

        @JvmStatic
        fun valueSymbolTexts(): List<String> = values().map { it.toSymbolTextOrNull()!! }

        @JvmSynthetic
        internal fun fromTimestamp(timestamp: Timestamp): TimestampPrecisionValue {
            return when (timestamp.precision!!) {
                Timestamp.Precision.YEAR -> Year
                Timestamp.Precision.MONTH -> Month
                Timestamp.Precision.DAY -> Day
                Timestamp.Precision.MINUTE -> Minute
                Timestamp.Precision.SECOND,
                Timestamp.Precision.FRACTION -> TimestampPrecisionValue(max(0, timestamp.decimalSecond.scale()))
            }
        }

        /**
         * The symbol text for this value as defined in the ISL specification, if one exists.
         */
        fun fromSymbolTextOrNull(symbolText: String): TimestampPrecisionValue? = when (symbolText) {
            "year" -> Year
            "month" -> Month
            "day" -> Day
            "minute" -> Minute
            "second" -> Second
            "millisecond" -> Millisecond
            "microsecond" -> Microsecond
            "nanosecond" -> Nanosecond
            else -> null
        }
    }

    /**
     * Returns the ISL symbol text for this [TimestampPrecisionValue], if one exists.
     */
    fun toSymbolTextOrNull(): String? = when (this) {
        Year -> "year"
        Month -> "month"
        Day -> "day"
        Minute -> "minute"
        Second -> "second"
        Millisecond -> "millisecond"
        Microsecond -> "microsecond"
        Nanosecond -> "nanosecond"
        else -> null
    }
}
