package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Type
import software.amazon.ionschema.Violations

internal interface TypeInternal : Type {
    fun isValidForBaseType(value: IonValue): Boolean

    fun validate(value: IonValue, issues: Violations)
}
