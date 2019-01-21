package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonValue
import software.amazon.ionschema.Constraint
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.Occurs.Companion.OPTIONAL
import software.amazon.ionschema.internal.util.ViolationChild
import software.amazon.ionschema.internal.util.Violations
import software.amazon.ionschema.internal.util.Violation
import software.amazon.ionschema.internal.util.CommonViolations

internal class Fields(
        ionValue: IonValue,
        private val schema: Schema
    ) : ConstraintBase(ionValue), Constraint {

    private val ionStruct: IonStruct

    init {
        if (ionValue.isNullValue || ionValue !is IonStruct || ionValue.size() == 0) {
            throw InvalidSchemaException(
                "fields must be a struct that defines at least one field ($ionValue)")
        }
        ionStruct = ionValue
        ionStruct.associateBy(
                { it.fieldName },
                { Occurs(it, schema, OPTIONAL) })
    }

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonStruct) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val fieldIssues = Violation(ion, "fields_mismatch", "one or more fields don't match expectations")
            val fieldConstraints = ionStruct.associateBy(
                    { it.fieldName },
                    { Pair(Occurs(it, schema, OPTIONAL, isField = true),
                           ViolationChild(path = it.fieldName))
                    })

            value.iterator().forEach {
                val pair = fieldConstraints[it.fieldName]
                if (pair != null) {
                    pair.first.validate(it, pair.second)
                } else {
                    // invalid if open content is false
                }
            }

            fieldConstraints.values.forEach { pair ->
                pair.first.validateAttempts(pair.second)
                if (!pair.second.isValid()) {
                    fieldIssues.add(pair.second)
                }
            }
            if (!fieldIssues.isValid()) {
                issues.add(fieldIssues)
            }
        }
    }
}
