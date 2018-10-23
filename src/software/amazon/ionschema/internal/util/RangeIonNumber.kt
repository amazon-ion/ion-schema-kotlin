package software.amazon.ionschema.internal.util

import software.amazon.ion.*
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.internal.util.Range.Companion.MAX
import software.amazon.ionschema.internal.util.Range.Companion.MIN
import java.math.BigDecimal

internal class RangeIonNumber(ion: IonList) : Range {
    companion object {
        private fun toBigDecimal(ion: IonValue) =
            when (ion) {
                is IonDecimal -> ion.bigDecimalValue()
                is IonFloat   -> ion.bigDecimalValue()
                is IonInt     -> BigDecimal(ion.bigIntegerValue())
                else          ->
                    throw InvalidSchemaException(
                            "Expected range min/max to be a decimal, float, or int (was $ion)")
            }
    }

    internal val min: Boundary
    internal val max: Boundary
    init {
        if (!ion.hasTypeAnnotation("range")) {
            throw InvalidSchemaException("Invalid range, missing 'range' annotation:  $ion")
        }
        if (ion.size != 2) {
            throw InvalidSchemaException("Invalid range, size of list must be 2:  $ion")
        }

        min = if (ion[0].equals(MIN)) {
            Boundary(null, Infinity.NEGATIVE)
            } else {
            Boundary(ion[0], Infinity.NEGATIVE)
            }
        max = if (ion[1].equals(MAX)) {
            Boundary(null, Infinity.POSITIVE)
            } else {
            Boundary(ion[1], Infinity.POSITIVE)
            }


        if (min.compareTo(max) > 0) {
            throw InvalidSchemaException("Min must be <= max in $ion")
        }
        if (min.value != null && max.value != null
                && min.value.equals(max.value)
                && (min.boundaryType == RangeBoundaryType.EXCLUSIVE
                    || max.boundaryType == RangeBoundaryType.EXCLUSIVE)) {
            throw InvalidSchemaException("No valid values in $ion")
        }
    }

    override fun contains(value: Int) = contains(BigDecimal(value))

    override fun contains(value: IonValue) = contains(toBigDecimal(value))

    private fun contains(value: BigDecimal) =
            min.compareTo(value) <= 0 && max.compareTo(value) >= 0

    override fun compareTo(value: Int) = -max.compareTo(BigDecimal(value))

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

        fun compareTo(other: BigDecimal) = when (value) {
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
