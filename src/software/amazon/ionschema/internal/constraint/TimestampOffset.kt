package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue

internal class TimestampOffset(
        private val ion: IonValue
    ) : ConstraintBase(ion) {

    override fun isValid(value: IonValue): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}