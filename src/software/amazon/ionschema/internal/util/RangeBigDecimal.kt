package software.amazon.ionschema.internal.util

import software.amazon.ion.*
import software.amazon.ionschema.InvalidSchemaException
import java.math.BigDecimal

internal class RangeBigDecimal(private val ion: IonList) : Range<BigDecimal> {
    companion object {
        private fun toBigDecimal(ion: IonValue) =
            when (ion) {
                is IonDecimal -> ion.bigDecimalValue()
                is IonFloat   -> ion.bigDecimalValue()
                is IonInt     -> BigDecimal(ion.bigIntegerValue())
                else          ->
                    throw InvalidSchemaException(
                            "Expected range lower/upper to be a decimal, float, or int (was $ion)")
            }
    }

    internal val lower: Boundary
    internal val upper: Boundary
    init {
        if (!ion.hasTypeAnnotation("range")) {
            throw InvalidSchemaException("Invalid range, missing 'range' annotation:  $ion")
        }
        if (ion.size != 2) {
            throw InvalidSchemaException("Invalid range, size of list must be 2:  $ion")
        }

        lower = if (ion[0] is IonSymbol && (ion[0] as IonSymbol).stringValue() == "min") {
                Boundary(null, Infinity.NEGATIVE)
            } else {
                Boundary(ion[0], Infinity.NEGATIVE)
            }
        upper = if (ion[1] is IonSymbol && (ion[1] as IonSymbol).stringValue() == "max") {
                Boundary(null, Infinity.POSITIVE)
            } else {
                Boundary(ion[1], Infinity.POSITIVE)
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
