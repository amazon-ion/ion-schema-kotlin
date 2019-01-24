package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonDecimal
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.internal.util.Range
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.CommonViolations

internal class Precision(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

    init {
        if (range.contains(0)) {
            throw InvalidSchemaException("Precision must be at least 1 ($ion)")
        }
    }

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonDecimal) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val precision = value.bigDecimalValue().precision()
            if (!range.contains(precision)) {
                issues.add(Violation(ion,
                        "invalid_precision",
                        "invalid precision %s, expected %s".format(precision, range)))
            }
        }
    }
}
