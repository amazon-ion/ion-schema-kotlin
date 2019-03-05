package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonTimestamp
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.CommonViolations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.Violations
import software.amazon.ionschema.internal.util.RangeFactory
import software.amazon.ionschema.internal.util.RangeType
import software.amazon.ionschema.internal.util.IonTimestampPrecision

internal class TimestampPrecision(
        ion: IonValue
) : ConstraintBase(ion) {

    private val range = RangeFactory.rangeOf<IonTimestamp>(ion, RangeType.ION_TIMESTAMP_PRECISION)

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonTimestamp) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            if (!range.contains(value)) {
                val actualPrecision = IonTimestampPrecision.toInt(value)
                issues.add(Violation(ion, "invalid_timestamp_precision",
                        "invalid timestamp precision %s, expected %s".format(actualPrecision, ion)))
            }
        }
    }
}

