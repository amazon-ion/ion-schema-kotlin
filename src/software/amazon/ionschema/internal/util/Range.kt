package software.amazon.ionschema.internal.util

import software.amazon.ion.IonList
import software.amazon.ion.IonValue

enum class RangeType {
    INT,
    INT_NON_NEGATIVE,
    ION_NUMBER,
    ION_TIMESTAMP_PRECISION,
}

internal interface Range<in T : Any> {
    fun contains(value: T): Boolean
}

internal class RangeFactory {
    companion object {
        @JvmStatic
        fun <T : Any> rangeOf(ion: IonValue, rangeType: RangeType): Range<T> {
            val ionList = if (ion !is IonList) {
                    val range = ion.system.newList(ion.clone(), ion.clone())
                    range.addTypeAnnotation("range")
                    range
                } else {
                    ion
                }

            @Suppress("UNCHECKED_CAST")
            return when (rangeType) {
                    RangeType.INT                     -> RangeInt(ionList)
                    RangeType.INT_NON_NEGATIVE        -> RangeIntNonNegative(ionList)
                    RangeType.ION_NUMBER              -> RangeIonNumber(ionList)
                    RangeType.ION_TIMESTAMP_PRECISION -> RangeIonTimestampPrecision(ionList)
                } as Range<T>
        }
    }
}

