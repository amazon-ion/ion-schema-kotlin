package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema
import software.amazon.ionschema.ViolationChild
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.CommonViolations

internal class Element(
        ion: IonValue,
        schema: Schema
    ) : ConstraintBase(ion) {

    private val typeReference = TypeReference(ion, schema, isField = true)

    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonContainer) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        } else {
            val elementIssues = Violation(ion, "element_mismatch", "one or more elements don't match expectations")
            value.forEachIndexed { idx, it ->
                val elementValidation = if (it.fieldName == null) {
                        ViolationChild(index = idx, value = it)
                    } else {
                        ViolationChild(fieldName = it.fieldName, value = it)
                    }
                typeReference.validate(it, elementValidation)
                if (!elementValidation.isValid()) {
                    elementIssues.add(elementValidation)
                }
            }
            if (!elementIssues.isValid()) {
                issues.add(elementIssues)
            }
        }
    }
}
