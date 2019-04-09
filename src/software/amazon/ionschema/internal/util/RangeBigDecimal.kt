package software.amazon.ionschema.internal.util

import software.amazon.ion.*
import software.amazon.ionschema.InvalidSchemaException
import java.math.BigDecimal

/**
 * Implementation of Range<BigDecimal>.  As of this writing, all other Range<T>
 * implementations directly or indirectly delegate to an instance of this class.
 */
internal class RangeBigDecimal(private val ion: IonList) : Range<BigDecimal> {
    companion object {
        private fun toBigDecimal(ion: IonValue) =
                if (ion.isNullValue) {
                    throw InvalidSchemaException("Unable to convert $ion to BigDecimal")
                } else {
                    when (ion) {
                        is IonDecimal -> ion.bigDecimalValue()
                        is IonFloat -> ion.bigDecimalValue()
                        is IonInt -> BigDecimal(ion.bigIntegerValue())
                        else ->
                            throw InvalidSchemaException(
                                    "Expected range lower/upper to be a decimal, float, or int (was $ion)")
                    }
                }
    }

    internal val lower: Boundary
    internal val upper: Boundary
    init {
        checkRange(ion)

        lower = when {
            isRangeMin(ion[0]) -> Boundary(null, Infinity.NEGATIVE)
            else  -> Boundary(ion[0], Infinity.NEGATIVE)
        }
        upper = when {
            isRangeMax(ion[1]) -> Boundary(null, Infinity.POSITIVE)
            else  -> Boundary(ion[1], Infinity.POSITIVE)
        }

        if (lower > upper) {
            throw InvalidSchemaException("Lower bound must be <= upper in $ion")
        }
        if (lower.value != null && upper.value != null
                && lower.value == upper.value
                && (lower.boundaryType == RangeBoundaryType.EXCLUSIVE
                    || upper.boundaryType == RangeBoundaryType.EXCLUSIVE)) {
            throw InvalidSchemaException("No valid values in $ion")
        }
    }

    override fun contains(value: BigDecimal) = lower <= value && upper >= value

    override fun toString() = ion.toString()

    internal enum class Infinity(val sign: Int) {
        NEGATIVE(-1),
        POSITIVE( 1),
    }

    /**
     * Represents either a lower or upper boundary of a range.
     */
    internal class Boundary(ion: IonValue?, private val infinity: Infinity) : Comparable<Boundary> {
        internal val value: BigDecimal?
        internal val boundaryType: RangeBoundaryType
        init {
            value = if (ion == null || ion.isNullValue) {
                boundaryType = RangeBoundaryType.INCLUSIVE
                null
            } else {
                boundaryType = RangeBoundaryType.forIon(ion)
                toBigDecimal(ion)
            }
        }

        operator fun compareTo(other: BigDecimal) = when (value) {
            null -> infinity.sign
            else -> {
                val compareResult = value.compareTo(other)
                when (compareResult) {
                    0 -> when (boundaryType) {
                        RangeBoundaryType.INCLUSIVE -> compareResult
                        RangeBoundaryType.EXCLUSIVE -> -infinity.sign
                    }
                    else -> compareResult
                }
            }
        }

        override fun compareTo(other: Boundary) =
            if (value != null) {
                if (other.value != null) {
                    value.compareTo(other.value)
                } else {
                    -other.infinity.sign
                }
            } else {
                if (other.value != null) {
                    infinity.sign
                } else {
                    if (infinity == other.infinity) {
                        0
                    } else {
                        infinity.sign
                    }
                }
            }
    }
}

