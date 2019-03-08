package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonList
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.CommonViolations

/**
 * Implements the contains constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#contains
 */
internal class Contains(
        ion: IonValue
) : ConstraintBase(ion) {

    private val expectedElements = if (ion !is IonList || ion.isNullValue) {
            throw InvalidSchemaException("Expected annotations as a list, found: $ion")
        } else {
            ion.toArray()
        }

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonContainer) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val expectedValues = expectedElements.toMutableSet()
            value.forEach {
                expectedValues.remove(it)
            }
            if (!expectedValues.isEmpty()) {
                issues.add(Violation(ion, "missing_values",
                        "missing value(s): " + expectedValues.joinToString { it.toString() }))
            }
        }
    }
}

