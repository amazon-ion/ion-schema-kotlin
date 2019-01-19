package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Violations

internal class TimestampOffset(
        ion: IonValue
    ) : ConstraintBase(ion) {

    override fun validate(value: IonValue, issues: Violations) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}