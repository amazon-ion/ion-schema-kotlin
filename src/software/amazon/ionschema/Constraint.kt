package software.amazon.ionschema

import software.amazon.ion.IonValue

/**
 * Represents a single constraint.
 */
interface Constraint {
    /**
     * Returns the name of the constraint.
     */
    fun name(): String

    /**
     * Checks this constraint against the provided value,
     * adding [Violation]s and/or [ViolationChild]ren to issues
     * if the constraint is violated.
     */
    fun validate(value: IonValue, issues: Violations)
}
