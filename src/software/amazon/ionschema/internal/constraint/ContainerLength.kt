package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Range

internal class ContainerLength(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

    override fun isValid(value: IonValue): Boolean {
        if (value is IonContainer && !value.isNullValue) {
            return range.contains(value.size())
        }
        return false
    }
}
