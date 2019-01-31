package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.CommonViolations
import software.amazon.ionschema.internal.util.RangeFactory
import software.amazon.ionschema.internal.util.RangeType

internal class CodepointLength(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = RangeFactory.rangeOf<Int>(ion, RangeType.INT_NON_NEGATIVE)

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonText) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val length = value.stringValue().length
            if (!range.contains(length)) {
                issues.add(Violation(ion, "invalid_codepoint_length",
                        "invalid codepoint length %s, expected %s".format(length, range)))
            }
        }
    }
}
