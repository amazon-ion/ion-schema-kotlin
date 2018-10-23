package software.amazon.ionschema

import software.amazon.ion.IonValue

interface ConstraintFactory {
    fun isConstraint(name: String): Boolean
    fun constraintFor(ion: IonValue, schema: Schema, type: Type?): Constraint
}
