package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.TypeInternal

interface Type {
    fun name(): String

    fun isValid(value: IonValue) = validate(this, value, true).isValid()

    fun validate(value: IonValue) = validate(this, value, false)

    private fun validate(type: Type, value: IonValue, shortCircuit: Boolean): Violations {
        val violations = Violations(
                shortCircuit = shortCircuit,
                childrenAllowed = false)
        try {
            (type as TypeInternal).validate(value, violations)
        } catch (e: ShortCircuitValidationException) {
            // short-circuit validation winds up here, safe to ignore
        }
        return violations
    }
}
