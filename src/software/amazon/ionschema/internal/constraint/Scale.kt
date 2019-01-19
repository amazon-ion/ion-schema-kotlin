package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonDecimal
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Range
import software.amazon.ionschema.internal.util.Violations
import software.amazon.ionschema.internal.util.Violation
import software.amazon.ionschema.internal.util.CommonViolations

internal class Scale(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

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
