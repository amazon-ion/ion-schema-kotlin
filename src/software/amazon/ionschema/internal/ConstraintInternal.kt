package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Violations

/**
 * Internal methods for interacting with [Constraint]s.
 */
internal interface ConstraintInternal : Constraint {
    val ion: IonValue

    /**
     * Checks this constraint against the provided value,
     * adding [Violation]s and/or [ViolationChild]ren to issues
     * if the constraint is violated.
     */
    fun validate(value: IonValue, issues: Violations)
}

