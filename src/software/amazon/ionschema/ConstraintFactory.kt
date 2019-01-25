package software.amazon.ionschema

import software.amazon.ion.IonValue

/**
 * Factory for [Constraint] objects.
 */
interface ConstraintFactory {
    /**
     * If [name] is a recognized constraint name, returns `true`, otherwise `false`.
     */
    fun isConstraint(name: String): Boolean

    /**
     * Instantiates a new [Constraint] as defined by [ion].
     */
    fun constraintFor(ion: IonValue, schema: Schema, type: Type?): Constraint
}
