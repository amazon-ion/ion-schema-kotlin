package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonValue
import software.amazon.ionschema.Constraint
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.RecurringTypeReference.Companion.OPTIONAL

internal class Fields(
        ionValue: IonValue,
        private val schema: Schema
    ) : ConstraintBase(ionValue), Constraint {

    private val ion: IonStruct

    init {
        if (ionValue.isNullValue || !(ionValue is IonStruct) || ionValue.size() == 0) {
            throw InvalidSchemaException(
                "fields must be a struct that defines at least one field ($ionValue)")
        }
        ion = ionValue
        ion.associateBy(
                { it.fieldName },
                { RecurringTypeReference(it, schema, OPTIONAL) })
    }

    override fun isValid(value: IonValue): Boolean {
        val fieldConstraints = ion.associateBy(
                { it.fieldName },
                { RecurringTypeReference(it, schema, OPTIONAL) })

        if (value is IonStruct && !value.isNullValue) {
            value.iterator().forEach {
                val rtr = fieldConstraints.get(it.fieldName)
                if (rtr != null) {
                    rtr.isValid(it)
                } else {
                    // invalid if OC is false
                }
            }

            var isValid = true
            fieldConstraints.values.forEach {
                if (!it.isValid()) {
                    if (isValid) {
                        isValid = false
                    }
                }
            }
            return isValid
        }
        return false
    }
}
