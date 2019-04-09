package software.amazon.ionschema.internal.util

import software.amazon.ion.IonList
import software.amazon.ionschema.InvalidSchemaException

/**
 * Implementation of Range<Int> restricted to non-negative integers.
 * Mostly delegates to RangeInt.
 */
internal class RangeIntNonNegative (
        private val ion: IonList,
        private val delegate: RangeInt = RangeInt(ion)
) : Range<Int> by delegate {

    init {
        if (!(compareValues(toInt(ion[0]), 0) >= 0 || isRangeMin(ion[0]))) {
            throw InvalidSchemaException("Invalid lower bound in positive int $ion")
        }

        if (!(compareValues(toInt(ion[1]), 0) >= 0 || isRangeMax(ion[1]))) {
            throw InvalidSchemaException("Invalid upper bound in positive int $ion")
        }
    }

    internal fun isAtMax(value: Int) = delegate.isAtMax(value)

    override fun toString() = delegate.toString()
}

