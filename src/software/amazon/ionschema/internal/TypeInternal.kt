package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Type

/**
 * Internal methods for interacting with [Type]s.
 */
internal interface TypeInternal : Type, ConstraintInternal {
    fun getBaseType(): TypeBuiltin

    fun isValidForBaseType(value: IonValue): Boolean
}

