package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Violations

/**
 * Represents a single constraint.
 */
internal interface Constraint {
    /**
     * The name of the constraint.
     */
    val name: String

    /**
     * Checks this constraint against the provided value,
     * adding [Violation]s and/or [ViolationChild]ren to issues
     * if the constraint is violated.
     */
    fun validate(value: IonValue, issues: Violations)
}

