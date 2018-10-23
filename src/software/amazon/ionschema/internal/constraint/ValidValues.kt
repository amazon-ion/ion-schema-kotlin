package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonList
import software.amazon.ion.IonSequence
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.internal.util.Range

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
        if (ion.isNullValue()) {
            throw InvalidSchemaException("$ion is not allowed in valid_values")
        } else if (ion.typeAnnotations.size > 0) {
            throw InvalidSchemaException("Annotations ($ion) are not allowed in valid_values")
        } else {
            true
        }

    override fun isValid(value: IonValue): Boolean {
        if (value.isNullValue) {
            return true
        }

        if (validRange != null) {
            return validRange.contains(value)
        }

        val valueWithoutAnnotations =
            if (value.typeAnnotations.size > 0) {
                val v = value.clone()
                v.clearTypeAnnotations()
                v
            } else {
                value
            }
        return validValues.contains(valueWithoutAnnotations)
    }
}
