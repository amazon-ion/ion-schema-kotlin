package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonLob
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Range

internal class ByteLength(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

    override fun isValid(value: IonValue)
            = value is IonLob
                && !value.isNullValue
                && range.contains(value.byteSize())
}
