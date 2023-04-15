package com.amazon.ionschema.model

import com.amazon.ion.Timestamp
import kotlin.math.abs

/**
 * Represents the offset of a [Timestamp] to be used as an argument for the `timestamp_offset` constraint.
 * @see Constraint.TimestampOffset
 */
sealed class TimestampOffsetValue {
    object Unknown : TimestampOffsetValue() {
        override fun toString(): String = "-00:00"
    }
    data class Known(val minutes: Int) : TimestampOffsetValue() {
        init {
            require(minutes in VALID_MINUTES_RANGE) { "timestamp offset cannot be more than 23h59m from zero" }
        }
        override fun toString(): String = "%s%02d:%02d".format(if (minutes < 0) '-' else '+', abs(minutes / 60), abs(minutes % 60))
    }

    companion object {
        private val REGEX = Regex("^[+-](2[0-3]|[01]\\d):[0-5]\\d$")
        private val VALID_MINUTES_RANGE = -1439..1439 // ±23h59m
        private val HOURS_SLICE = 1..2
        private val MINUTES_SLICE = 4..5

        /**
         * Parses a timestamp offset string into a [TimestampOffsetValue].
         */
        @JvmStatic
        fun parse(string: String): TimestampOffsetValue {
            require(REGEX.matches(string)) { "timestamp offset value '$string' does not match required format '$REGEX'" }
            return if (string == "-00:00") {
                Unknown
            } else {
                val sign = if (string[0] == '-') -1 else 1
                val hours = string.slice(HOURS_SLICE).toInt()
                val minutes = string.slice(MINUTES_SLICE).toInt()
                Known(sign * (hours * 60 + minutes))
            }
        }

        /**
         * Creates a [TimestampOffsetValue] for the given number of minutes.
         * If minutes is `null`, returns [TimestampOffsetValue.Unknown].
         */
        @JvmStatic
        fun fromMinutes(minutes: Int?): TimestampOffsetValue {
            return minutes?.let { Known(it) } ?: Unknown
        }
    }
}
