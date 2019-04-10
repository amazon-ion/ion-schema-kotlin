package software.amazon.ionschema

import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.Constraint

/**
 * A Type consists of an optional name and zero or more constraints.
 *
 * Unless otherwise specified, the constraint `type: any` is automatically applied.
 */
interface Type {
    /**
     * The name of the type;  if the type has no name, a string representing
     * the definition of the type is returned.
     */
    val name: String

    /**
     * If the specified value violates one or more of this type's constraints,
     * returns `false`, otherwise `true`.
     */
    fun isValid(value: IonValue): Boolean = validate(this, value, true).isValid()

    /**
     * Returns a Violations object indicating whether the specified value
     * is valid for this type, and if not, provides details as to which
     * constraints were violated.
     */
    fun validate(value: IonValue): Violations = validate(this, value, false)

    private fun validate(type: Type, value: IonValue, shortCircuit: Boolean): Violations {
        val violations = Violations(
                shortCircuit = shortCircuit,
                childrenAllowed = false)
        try {
            (type as Constraint).validate(value, violations)
        } catch (e: ShortCircuitValidationException) {
            // short-circuit validation winds up here, safe to ignore
        }
        return violations
    }
}

