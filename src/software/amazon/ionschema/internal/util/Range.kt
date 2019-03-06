package software.amazon.ionschema.internal.util

import software.amazon.ion.IonList
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException

enum class RangeType {
    INT,
    INT_NON_NEGATIVE,
    ION_NUMBER,
    ION_TIMESTAMP,
    ION_TIMESTAMP_PRECISION,
}

internal interface Range<in T : Any> {
    fun contains(value: T): Boolean
}

internal class RangeFactory {
    companion object {
        @JvmStatic
        fun <T : Any> rangeOf(ion: IonValue, rangeType: RangeType): Range<T> {
            if (ion.isNullValue) {
                throw InvalidSchemaException("Invalid range $ion")
            }

            val ionList = if (ion !is IonList) {
                    val range = ion.system.newList(ion.clone(), ion.clone())
                    range.addTypeAnnotation("range")
                    range
                } else {
                    ion
                }

            if (ionList.size != 2 || ionList[0].isNullValue || ionList[1].isNullValue
                    || ((ionList[0] as? IonSymbol)?.stringValue() == "min"
                            && ((ionList[1] as? IonSymbol)?.stringValue() == "max"))) {
                throw InvalidSchemaException("Invalid range $ion")
            }

            @Suppress("UNCHECKED_CAST")
            return when (rangeType) {
                    RangeType.INT                     -> RangeInt(ionList)
                    RangeType.INT_NON_NEGATIVE        -> RangeIntNonNegative(ionList)
                    RangeType.ION_NUMBER              -> RangeIonNumber(ionList)
                    RangeType.ION_TIMESTAMP           -> RangeIonTimestamp(ionList)
                    RangeType.ION_TIMESTAMP_PRECISION -> RangeIonTimestampPrecision(ionList)
                } as Range<T>
        }
    }
}

