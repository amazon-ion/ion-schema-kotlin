package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonDecimal
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.internal.util.Range

internal class Precision(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

    init {
        if (range.contains(0)) {
            throw InvalidSchemaException("Precision must be at least 1 ($ion)")
        }
    }

    override fun isValid(value: IonValue)
            = value is IonDecimal
                && !value.isNullValue
                && range.contains(value.bigDecimalValue().precision())
}
