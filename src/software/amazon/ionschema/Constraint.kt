package software.amazon.ionschema

import software.amazon.ion.IonValue

interface Constraint {
    fun name(): String
    fun validate(value: IonValue, issues: Violations)
}
