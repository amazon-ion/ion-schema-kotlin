package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonDecimal
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.CommonViolations
import software.amazon.ionschema.internal.util.RangeFactory
import software.amazon.ionschema.internal.util.RangeType

/**
 * Implements the precision constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#precision
 */
internal class Precision(
        ion: IonValue
) : ConstraintBase(ion) {

    private val range = RangeFactory.rangeOf<Int>(ion, RangeType.INT_NON_NEGATIVE)

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

