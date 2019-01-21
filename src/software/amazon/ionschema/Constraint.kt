package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Violations

interface Constraint {
    fun name(): String
    fun validate(value: IonValue, issues: Violations)
}
