package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonLob
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.util.Range
import software.amazon.ionschema.internal.util.Violations
import software.amazon.ionschema.internal.util.Violation
import software.amazon.ionschema.internal.util.CommonViolations

internal class ByteLength(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val range = Range.rangeOf(ion, Range.RangeType.POSITIVE_INTEGER)

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonLob) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val length = value.byteSize()
            if (!range.contains(length)) {
                issues.add(Violation(ion,
                        "invalid_byte_length",
                        "invalid byte length %s, expected %s".format(length, range)))
            }
        }
    }
}
