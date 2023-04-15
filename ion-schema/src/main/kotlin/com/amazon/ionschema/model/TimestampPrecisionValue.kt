package com.amazon.ionschema.model

/**
 * Represents the available arguments for the `timestamp_precision` constraint.
 * @see Constraint.TimestampPrecision
 */
enum class TimestampPrecisionValue(private val id: Int) {
    Year(-4),
    Month(-3),
    Day(-2),
    // hour (without minute) is not supported by Ion
    Minute(-1),
    Second(0),
    Millisecond(3),
    Microsecond(6),
    Nanosecond(9);

    /**
     * The symbol text for this value as defined in the ISL specification.
     */
    val symbolText = name.toLowerCase()

    companion object {
        /**
         * Returns the [TimestampPrecisionValue] corresponding to `text`, or `null` if `text` does not correspond to any value.
         */
        @JvmStatic
        fun fromSymbolTextOrNull(text: String): TimestampPrecisionValue? {
            return values().firstOrNull { it.symbolText == text }
        }

        /**
         * Returns the [TimestampPrecisionValue] corresponding to `text`.
         * @throws IllegalArgumentException if `text` does not correspond to any value.
         */
        @JvmStatic
        fun fromSymbolText(text: String): TimestampPrecisionValue {
            return fromSymbolTextOrNull(text) ?: throw IllegalArgumentException("'$text' is not a timestamp precision value")
        }
    }
}
