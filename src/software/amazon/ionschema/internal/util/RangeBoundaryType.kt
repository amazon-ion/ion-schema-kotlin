package software.amazon.ionschema.internal.util

import software.amazon.ion.IonValue

internal enum class RangeBoundaryType {
    EXCLUSIVE,
    INCLUSIVE;

    companion object {
        fun forIon(ion: IonValue) =
                if (ion.hasTypeAnnotation("exclusive")) {
                    EXCLUSIVE
                } else {
                    INCLUSIVE
                }
    }
}
