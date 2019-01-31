package software.amazon.ionschema.internal.util

import software.amazon.ion.IonList
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonTimestamp
import software.amazon.ion.Timestamp
import software.amazon.ionschema.InvalidSchemaException

/**
 *
 * This implementation simply translates the allowed timestamp precisions
 * to a Range<Int>.
 */
internal class RangeIonTimestampPrecision (
        ion: IonList
) : Range<IonTimestamp> {

    private val delegate: Range<Int>

    init {
        if (!ion.hasTypeAnnotation("range")) {
            throw InvalidSchemaException("Invalid timestamp range, missing 'range' annotation:  $ion")
        }
        if (ion.size != 2) {
            throw InvalidSchemaException("Invalid timestamp range, size of list must be 2:  $ion")
        }

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
                throw InvalidSchemaException("Invalid timestamp range $end boundary:  $ionValue")
            }
        }
        delegate = RangeFactory.rangeOf<Int>(intRangeIon, RangeType.INT)
    }

    override fun contains(value: IonTimestamp) = delegate.contains(IonTimestampPrecision.toInt(value))
}

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
                    Timestamp.Precision.SECOND -> ion.timestampValue().decimalSecond.scale()
                    null -> throw NullPointerException()
                }
    }
}

