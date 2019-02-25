package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Type

internal interface TypeInternal : Type {
    fun isValidForBaseType(value: IonValue): Boolean
}

