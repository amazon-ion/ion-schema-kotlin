package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Validator

interface Type {
    fun name(): String

    fun isValid(value: IonValue) = Validator.isValid(this, value)

    fun validate(value: IonValue) = Validator.validate(this, value)
}
