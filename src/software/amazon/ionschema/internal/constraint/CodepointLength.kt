package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Range

internal class CodepointLength(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

    override fun isValid(value: IonValue)
            = range.contains((value as IonText).stringValue().length)
}
