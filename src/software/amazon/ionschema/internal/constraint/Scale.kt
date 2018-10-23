package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonDecimal
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Range

internal class Scale(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

    override fun isValid(value: IonValue)
            = value is IonDecimal
                && !value.isNullValue
                && range.contains(value.bigDecimalValue().scale())
}
