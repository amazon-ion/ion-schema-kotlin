package software.amazon.ionschema

import software.amazon.ion.IonValue

interface Constraint {
    fun name(): String
    fun isValid(value: IonValue): Boolean
}
