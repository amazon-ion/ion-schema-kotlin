package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.Constraint
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.Occurs.Companion.OPTIONAL
import software.amazon.ionschema.ViolationChild
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.CommonViolations

/**
 * Implements the fields constraint.
 *
 * [Content] and [Occurs] constraints in the context of a struct are also
 * handled by this class.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#fields
 */
internal class Fields(
        ionValue: IonValue,
        private val schema: Schema
) : ConstraintBase(ionValue), Constraint {

    private val ionStruct: IonStruct
    private val contentConstraintIon: IonValue?
    private val contentClosed: Boolean

    init {
        if (ionValue.isNullValue || ionValue !is IonStruct || ionValue.size() == 0) {
            throw InvalidSchemaException(
                "fields must be a struct that defines at least one field ($ionValue)")
        }
        ionStruct = ionValue
        ionStruct.associateBy(
                { it.fieldName },
                { Occurs(it, schema, OPTIONAL) })

        contentConstraintIon = (ionStruct.container as? IonStruct)?.get("content") as? IonSymbol
        contentClosed = contentConstraintIon?.stringValue().equals("closed")
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
                            ViolationChild(fieldName = it.fieldName))
                    })
            var closedContentIssues: Violation? = null

            value.iterator().forEach {
                val pair = fieldConstraints[it.fieldName]
                if (pair != null) {
                    pair.first.validate(it, pair.second)
                } else if (contentClosed) {
                    if (closedContentIssues == null) {
                        closedContentIssues = Violation(contentConstraintIon,
                                "unexpected_content", "found one or more unexpected fields")
                        issues.add(closedContentIssues!!)
                    }
                    closedContentIssues!!.add(ViolationChild(it.fieldName, value = it))
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

