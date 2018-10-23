package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonTimestamp
import software.amazon.ion.IonValue
import software.amazon.ion.Timestamp

internal class TimestampPrecision(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = TimestampPrecisionRange(ion)

    override fun isValid(value: IonValue)
            = value is IonTimestamp
                && !value.isNullValue
                && range.contains(Precision.precisionOf(value))

    private enum class Precision {
        year,
        month,
        day,
        minute,
        second,
        millisecond,
        microsecond,
        nanosecond;

        companion object {
            fun precisionOf(ion: IonTimestamp): Precision =
                when (ion.timestampValue().precision) {
                    Timestamp.Precision.YEAR -> year
                    Timestamp.Precision.MONTH -> month
                    Timestamp.Precision.DAY -> day
                    Timestamp.Precision.MINUTE -> minute
                    Timestamp.Precision.SECOND -> {
                        when (ion.timestampValue().decimalMillis.precision()) {
                            0 -> second
                            3 -> millisecond
                            6 -> microsecond
                            9 -> nanosecond
                            else -> throw RuntimeException("Precision of timestamp '$ion' is not supported")
                        }
                    }
                }
        }
    }

    private class TimestampPrecisionRange(ion: IonValue) {
        //RangeBoundaryType

        fun contains(value: Precision): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
