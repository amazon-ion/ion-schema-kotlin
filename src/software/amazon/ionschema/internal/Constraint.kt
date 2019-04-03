package software.amazon.ionschema.internal

import software.amazon.ion.IonValue

/**
 * Represents a single constraint.
 */
internal interface Constraint {
    /**
     * The name of the constraint.
     */
    val name: String
}

