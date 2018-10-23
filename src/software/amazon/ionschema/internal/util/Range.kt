package software.amazon.ionschema.internal.util

import software.amazon.ion.*
import software.amazon.ionschema.internal.ION
import software.amazon.ionschema.InvalidSchemaException

internal interface Range {
    enum class RangeType {
        NUMBER,
        POSITIVE_INTEGER,
    }

    companion object {
        internal val MIN = ION.newSymbol("min")
        internal val MAX = ION.newSymbol("max")

        fun rangeOf(ion: IonValue, rangeType: RangeType): Range =
            when (rangeType) {
                RangeType.NUMBER -> when (ion) {
                    is IonList -> RangeIonNumber(ion)
                    is IonDecimal, is IonFloat, is IonInt -> RangeIonNumber(convertToRange(ion))
                    else -> throw InvalidSchemaException("Invalid numeric range: $ion")
                }
                RangeType.POSITIVE_INTEGER -> when (ion) {
                    is IonList -> RangeIonPosInt(ion)
                    is IonDecimal, is IonFloat, is IonInt -> RangeIonPosInt(convertToRange(ion))
                    else -> throw InvalidSchemaException("Invalid numeric range: $ion")
                }
            }

        private fun convertToRange(ion: IonValue): IonList {
            val range = ION.newList(ion.clone(), ion.clone())
            range.addTypeAnnotation("range")
            return range
        }
    }

    fun contains(value: Int): Boolean
    fun contains(value: IonValue): Boolean
    fun compareTo(value: Int): Int
}
