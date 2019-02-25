package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.ConstraintInternal

internal abstract class ConstraintBase(
        override val ion: IonValue
) : ConstraintInternal {

    override fun name() = ion.fieldName

    override fun toString() = ion.toString()
}

