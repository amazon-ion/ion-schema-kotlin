package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Constraint
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type

/**
 * Factory for [Constraint] objects.
 */
internal interface ConstraintFactory {
    /**
     * If [name] is a recognized constraint name, returns `true`, otherwise `false`.
     */
    fun isConstraint(name: String): Boolean

    /**
     * Instantiates a new [Constraint] as defined by [ion].
     */
    fun constraintFor(ion: IonValue, schema: Schema, type: Type?): Constraint
}
