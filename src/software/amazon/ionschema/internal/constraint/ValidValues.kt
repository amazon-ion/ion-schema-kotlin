package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonList
import software.amazon.ion.IonSequence
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.internal.util.Range
import software.amazon.ionschema.internal.util.Violations
import software.amazon.ionschema.internal.util.Violation
import software.amazon.ionschema.internal.util.withoutTypeAnnotations

internal class ValidValues(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val validRange: Range?
    private val validValues: Set<IonValue>

    init {
        validRange =
            if (ion is IonList && ion.hasTypeAnnotation("range")) {
                Range.rangeOf(ion, Range.RangeType.NUMBER)
            } else {
                null
            }

        validValues = if (validRange != null) {
                setOf()
            } else {
                when (ion) {
                    is IonSequence -> ion.filter { checkValue(it) }.toSet()
                    else -> {
                        checkValue(ion)
                        setOf(ion)
                    }
                }
            }
    }

    private fun checkValue(ion: IonValue) =
        if (ion.typeAnnotations.size > 0) {
            throw InvalidSchemaException("Annotations ($ion) are not allowed in valid_values")
        } else {
            true
        }

    override fun validate(value: IonValue, issues: Violations) {
        if (validRange != null) {
            if (!validRange.contains(value)) {
                issues.add(Violation(ion, "invalid_value", "invalid value $value"))
            }
        } else {
            val v = value.withoutTypeAnnotations()
            if (!validValues.contains(v)) {
                issues.add(Violation(ion, "invalid_value", "invalid value $v"))
            }
        }
    }
}
