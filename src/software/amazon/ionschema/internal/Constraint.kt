package software.amazon.ionschema.internal

import software.amazon.ion.IonValue

/**
 * Represents a single constraint.
 */
internal interface Constraint {
    /**
     * Returns the name of the constraint.
     */
    fun name(): String
}

