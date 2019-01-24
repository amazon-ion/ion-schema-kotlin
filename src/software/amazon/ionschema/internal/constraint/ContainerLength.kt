package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Range
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.CommonViolations

internal class ContainerLength(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonContainer) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val length = value.size()
            if (!range.contains(length)) {
                issues.add(Violation(ion, "invalid_container_length",
                        "invalid container length %s, expected %s".format(length, range)))
            }
        }
    }
}
