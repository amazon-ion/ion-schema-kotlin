package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonList
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.ConstraintInternal
import software.amazon.ionschema.internal.TypeReference

internal abstract class LogicConstraints(
        ion: IonValue,
        schema: Schema
    ) : ConstraintBase(ion) {

    internal val types = (ion as IonList).map { TypeReference.create(it, schema) }

    internal fun validateTypes(value: IonValue, issues: Violations): List<Type> {
        val validTypes = mutableListOf<Type>()
        types.forEach {
            val checkpoint = issues.checkpoint()
            it().validate(value, issues)
            if (checkpoint.isValid()) {
                validTypes.add(it())
            }
        }
        return validTypes
    }
}

internal class AllOf(ion: IonValue, schema: Schema) : LogicConstraints(ion, schema) {
    override fun validate(value: IonValue, issues: Violations) {
        val allOfViolation = Violation(ion, "all_types_not_matched")
        val count = validateTypes(value, allOfViolation).size
        if (count != types.size) {
            allOfViolation.message = "value matches $count types, expected ${types.size}"
            issues.add(allOfViolation)
        }
    }
}

internal class AnyOf(ion: IonValue, schema: Schema) : LogicConstraints(ion, schema) {
    override fun validate(value: IonValue, issues: Violations) {
        val anyOfViolation = Violation(ion, "no_types_matched", "value matches none of the types")
        val count = validateTypes(value, anyOfViolation).size
        if (count == 0) {
            issues.add(anyOfViolation)
        }
    }
}

internal class OneOf(ion: IonValue, schema: Schema) : LogicConstraints(ion, schema) {
    override fun validate(value: IonValue, issues: Violations) {
        val oneOfViolation = Violation(ion)
        val validTypes = validateTypes(value, oneOfViolation)
        if (validTypes.size != 1) {
            if (validTypes.size == 0) {
                oneOfViolation.code = "no_types_matched"
                oneOfViolation.message = "value matches none of the types"
            }
            if (validTypes.size > 1) {
                oneOfViolation.code = "more_than_one_type_matched"
                oneOfViolation.message = "value matches %s types, expected 1".format(validTypes.size)

                validTypes.forEach {
                    val typeDef = (it as ConstraintInternal).ion
                    oneOfViolation.add(
                            Violation(typeDef, "type_matched",
                                    "value matches type %s".format(typeDef)))
                }
            }
            issues.add(oneOfViolation)
        }
    }
}

internal class Not(ion: IonValue, schema: Schema) : ConstraintBase(ion) {
    private val type = TypeReference.create(ion, schema)

    override fun validate(value: IonValue, issues: Violations) {
        val child = Violation(ion, "type_matched", "value unexpectedly matches type")
        type().validate(value, child)
        if (child.isValid()) {
            issues.add(child)
        }
    }
}

