package com.amazon.ionschema.model

/**
 * Represents the IEEE-754 interchange formats supported by the `ieee754_float` constraint.
 * See [`ieee754_float`](https://amazon-ion.github.io/ion-schema/docs/isl-2-0/spec#ieee754_float) in the ISL 2.0 specification.
 * See also [IEEE-754](https://en.wikipedia.org/wiki/IEEE_754) on Wikipedia for more information about the different formats.
 */
enum class Ieee754InterchangeFormat {
    Binary16,
    Binary32,
    Binary64;

    /**
     * The symbol text for this value as defined in the ISL specification.
     */
    val symbolText = name.toLowerCase()

    companion object {
        /**
         * Returns the [Ieee754InterchangeFormat] corresponding to `text`, or `null` if `text` does not correspond to any value.
         */
        @JvmStatic
        fun fromSymbolTextOrNull(text: String): Ieee754InterchangeFormat? {
            return values().firstOrNull { it.symbolText == text }
        }

        /**
         * Returns the [Ieee754InterchangeFormat] corresponding to `text`.
         * @throws IllegalArgumentException if `text` does not correspond to any value.
         */
        @JvmStatic
        fun fromSymbolText(text: String): Ieee754InterchangeFormat {
            return fromSymbolTextOrNull(text)
                ?: throw IllegalArgumentException("'$text' is not a supported ieee754 format value")
        }
    }
}
