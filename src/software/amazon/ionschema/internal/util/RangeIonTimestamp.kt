package software.amazon.ionschema.internal.util

import software.amazon.ion.IonList
import software.amazon.ion.IonTimestamp
import software.amazon.ionschema.InvalidSchemaException

/**
 * Implementation of Range<IonTimestamp> which mostly delegates to RangeBigDecimal.
 */
internal class RangeIonTimestamp private constructor (
        private val delegate: RangeBigDecimal
) : Range<IonTimestamp> {

    constructor (ion: IonList) : this(toRangeBigDecimal(ion))

    companion object {
        private fun toRangeBigDecimal(ion: IonList): RangeBigDecimal {
            checkRange(ion)

            // convert to a decimal range
            val newRange = ion.system.newEmptyList()
            newRange.addTypeAnnotation("range")
            ion.forEach { ionValue ->
                val newValue = if (ionValue is IonTimestamp) {
                    if (ionValue.localOffset == null) {
                        throw InvalidSchemaException(
                                "Timestamp range bound doesn't specify a local offset: $ionValue")
                    }
                    ion.system.newDecimal(ionValue.decimalMillis)
                } else {
                    ionValue.clone()
                }
                ionValue.typeAnnotations.forEach { newValue.addTypeAnnotation(it) }
                newRange.add(newValue)
            }

            return RangeBigDecimal(newRange)
        }
    }

    override fun contains(value: IonTimestamp): Boolean {
        // ValidValues performs this same check and adds a Violation
        // instead of invoking this method;  this if is here purely
        // as a defensive safety check, and will ideally never be true
        if (value.localOffset == null) {
            throw IllegalArgumentException("Unable to compare timestamp with unknown local offset: $value")
        }
        return delegate.contains(value.decimalMillis)
    }
}

