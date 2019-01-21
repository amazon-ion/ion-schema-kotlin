package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue
import software.amazon.ionschema.Constraint

internal abstract class ConstraintBase(
        internal val ion: IonValue
    ) : Constraint {

    override fun name() = ion.fieldName

    override fun toString(): String {
        val sb = StringBuilder()
        //sb.append(name()).append(": ")
        sb.append(ion)
        return sb.toString()
    }
}
