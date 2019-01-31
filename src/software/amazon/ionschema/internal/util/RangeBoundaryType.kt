package software.amazon.ionschema.internal.util

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException

internal enum class RangeBoundaryType {
    EXCLUSIVE,
    INCLUSIVE;

    companion object {
        fun forIon(ion: IonValue): RangeBoundaryType {
            val isExclusive = ion.hasTypeAnnotation("exclusive")
            return if (ion is IonSymbol && (ion.stringValue() == "min" || ion.stringValue() == "max")) {
                       if (isExclusive) {
                           throw InvalidSchemaException("Invalid range bound '$ion'")
                       }
                       INCLUSIVE
                   } else if (isExclusive) {
                       EXCLUSIVE
                   } else {
                       INCLUSIVE
                   }
        }
    }
}

