package software.amazon.ionschema.internal.util

import software.amazon.ion.IonValue

/**
 * IonValue extension functions
 */
internal fun IonValue.withoutTypeAnnotations() =
        if (typeAnnotations.isNotEmpty()) {
            clone().apply { clearTypeAnnotations() }
        } else {
            this
        }

