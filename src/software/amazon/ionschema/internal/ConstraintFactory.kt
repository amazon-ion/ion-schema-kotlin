package software.amazon.ionschema.internal

import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema

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
     * @param[ion] IonValue identifying the constraint to construct as well as its configuration
     * @param[schema] passed to constraints that require a schema object
     */
    fun constraintFor(ion: IonValue, schema: Schema): Constraint
}

