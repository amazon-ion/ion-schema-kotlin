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
}

