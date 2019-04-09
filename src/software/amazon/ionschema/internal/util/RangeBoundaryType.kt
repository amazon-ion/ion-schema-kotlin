package software.amazon.ionschema.internal.util

import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException

/**
 * Enum indicating whether an upper or lower range boundary is
 * inclusive or exclusive.
 */
internal enum class RangeBoundaryType {
    EXCLUSIVE,
    INCLUSIVE;

    companion object {
        fun forIon(ion: IonValue): RangeBoundaryType {
            val isExclusive = ion.hasTypeAnnotation("exclusive")
            return when {
                isRangeMin(ion) || isRangeMax(ion) -> {
                    if (isExclusive) {
                        throw InvalidSchemaException("Invalid range bound '$ion'")
                    }
                    INCLUSIVE
                }
                isExclusive -> EXCLUSIVE
                else -> INCLUSIVE
            }
        }
    }
}

