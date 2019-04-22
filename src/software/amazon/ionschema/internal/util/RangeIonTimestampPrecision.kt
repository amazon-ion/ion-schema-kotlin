package software.amazon.ionschema.internal.util

import software.amazon.ion.IonList
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonTimestamp
import software.amazon.ion.Timestamp
import software.amazon.ionschema.InvalidSchemaException

/**
 * Implementation of Range<IonTimestamp> that validates that the precision
 * of a given timestamp is within the expected range.
 *
 * This implementation simply translates the allowed timestamp precisions
 * to a Range<Int>.
 *
 * @see IonTimestampPrecision
 */
internal class RangeIonTimestampPrecision (
        ion: IonList
) : Range<IonTimestamp> {

    private val delegate: Range<Int>

    init {
        checkRange(ion)

        // convert to an int range
        // e.g., range::[year, exclusive::millisecond] is translated to range::[-4, exclusive::3]
        val intRangeIon = ion.system.newEmptyList()
        intRangeIon.addTypeAnnotation("range")
        ion.forEachIndexed { index, ionValue ->
            val isValid = try {
                    if (ionValue is IonSymbol && !ionValue.isNullValue) {
                        val itp = IonTimestampPrecision.valueOf(ionValue.stringValue())
                        val ionInt = when (ionValue.stringValue()) {
                            "min" -> ion.system.newInt(Int.MIN_VALUE)
                            "max" -> ion.system.newInt(Int.MAX_VALUE)
                            else -> ion.system.newInt(itp.id)
                        }
                        ionValue.typeAnnotations.forEach { ionInt.addTypeAnnotation(it) }
                        intRangeIon.add(ionInt)
                        true
                    } else {
                        false
                    }
                } catch (e: IllegalArgumentException) {
                    false
                }

            if (!isValid) {
                val end = if (index == 0) {
                    "lower"
                } else {
                    "upper"
                }
                throw InvalidSchemaException("Invalid timestamp range $end bound:  $ionValue")
            }
        }
        delegate = RangeFactory.rangeOf<Int>(intRangeIon, RangeType.INT)
    }

    override fun contains(value: IonTimestamp) = delegate.contains(IonTimestampPrecision.toInt(value))
}

/**
 * Maps timestamp precisions to int values.  Note that RangeIonTimestampPrecision
 * delegates to a Range<Int> instance, and:
 * * 'year' is the minimum timestamp precision, so it should correspond to the lowest int
 * * 'nanosecond' is the maximum timestamp precision enum value, and we should allow room for
 *   a 'picosecond' value in the future;  using the scale of Ion's Timestamp.decimalSecond serves
 *   this purpose well when the precision is 'second' or finer-grained
 * * if 'second' is anchored at 0, then less precise enum values simply map to negative ints,
 *   and no additional offset calculation is required
 */
internal enum class IonTimestampPrecision (val id: Int) {
    year(-4),
    month(-3),
    day(-2),
    // hour (without minute) is not supported by Ion
    minute(-1),
    second(0),
    millisecond(3),
    microsecond(6),
    nanosecond(9);

    companion object {
        fun toInt(ion: IonTimestamp): Int =
                when (ion.timestampValue().precision) {
                    Timestamp.Precision.YEAR -> year.id
                    Timestamp.Precision.MONTH -> month.id
                    Timestamp.Precision.DAY -> day.id
                    Timestamp.Precision.MINUTE -> minute.id
                    Timestamp.Precision.SECOND, Timestamp.Precision.FRACTION -> ion.timestampValue().decimalSecond.scale()
                    null -> throw NullPointerException()
                }
    }
}

