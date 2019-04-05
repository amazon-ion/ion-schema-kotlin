package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonTimestamp
import software.amazon.ion.IonValue
import software.amazon.ionschema.Violation
import software.amazon.ionschema.Violations
import software.amazon.ionschema.internal.CommonViolations
import software.amazon.ionschema.internal.util.RangeFactory
import software.amazon.ionschema.internal.util.RangeType
import software.amazon.ionschema.internal.util.IonTimestampPrecision

/**
 * Implements the timestamp_precision constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#timestamp_precision
 */
internal class TimestampPrecision(
        ion: IonValue
) : ConstraintBase(ion) {

    private val range = RangeFactory.rangeOf<IonTimestamp>(ion, RangeType.ION_TIMESTAMP_PRECISION)

    override fun validate(value: IonValue, issues: Violations) {
        validateAs<IonTimestamp>(value, issues) { v ->
            if (!range.contains(v)) {
                val actualPrecision = IonTimestampPrecision.toInt(v)
                issues.add(Violation(ion, "invalid_timestamp_precision",
                        "invalid timestamp precision %s, expected %s".format(actualPrecision, ion)))
            }
        }
    }
}

