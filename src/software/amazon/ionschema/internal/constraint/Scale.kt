package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonDecimal
import software.amazon.ion.IonValue
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.CommonViolations
import software.amazon.ionschema.internal.util.RangeFactory
import software.amazon.ionschema.internal.util.RangeType

/**
 * Implements the scale constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#scale
 */
internal class Scale(
        ion: IonValue
) : ConstraintBase(ion) {

    private val range = RangeFactory.rangeOf<Int>(ion, RangeType.INT_NON_NEGATIVE)

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonDecimal) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val scale = value.bigDecimalValue().scale()
            if (!range.contains(scale)) {
                issues.add(Violation(ion, "invalid_scale",
                        "invalid scale %s, expected %s".format(scale, range)))
            }
        }
    }
}

