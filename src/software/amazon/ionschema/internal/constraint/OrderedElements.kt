package software.amazon.ionschema.internal.constraint

import software.amazon.ion.*
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.Occurs.Companion.REQUIRED
import software.amazon.ionschema.internal.util.ViolationChild
import software.amazon.ionschema.internal.util.Violations
import software.amazon.ionschema.internal.util.Violation
import software.amazon.ionschema.internal.util.CommonViolations

internal class OrderedElements(
        ion: IonValue,
        private val schema: Schema
    ) : ConstraintBase(ion) {

    init {
        (ion as IonList).map { Occurs(it, schema, REQUIRED) }
    }

    // TBD this impl does not backtrack
    override fun validate(value: IonValue, issues: Violations) {
        if (value !is IonSequence) {
            issues.add(CommonViolations.INVALID_TYPE(ion, value))
            return
        }

        if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
            return
        }

        val types = (ion as IonList).map {
            Occurs(it, schema, REQUIRED)
        }.toList()

        var typeIdx = 0
        val elements = value
        var elementIdx = 0

        val violation = Violation(ion, "ordered_elements_mismatch",
                "one or more ordered elements don't match specification")

        while (true) {
            if (typeIdx == types.size) {
                // we've exhausted the types, have we exhausted the elements?
                if (elementIdx != elements.size) {
                    val v = Violation(ion, "unexpected_content", "unexpected content for ordered elements")
                    for (i in elementIdx..(elements.size - 1)) {
                        v.add(ViolationChild(index = i, value = elements[i]))
                    }
                    issues.add(v)
                }
                if (!violation.isValid()) {
                    issues.add(violation)
                }
                return   // either way, we've gone as far as we can
            }

            val type = types[typeIdx]

            if (elementIdx == elements.size) {
                // we've exhausted the elements, did we meet the type's "occurs" requirements?
                if (!type.isValidCountWithinRange()) {
                    val elementValidation = ViolationChild(index = elementIdx)
                    elementValidation.add(
                            Violation(type.occursIon,
                                "occurs_mismatch",
                                "expected %s occurrences, found %s".format(type.range, type.validCount)))
                    violation.add(elementValidation)
                }

                // keep going...there may be other types that expect to be matched
                typeIdx++

            } else {
                val elementValidation = ViolationChild(index = elementIdx, value = elements[elementIdx])
                type.validate(elements[elementIdx], elementValidation)

                if (elementValidation.isValid()) {
                    violation.removeChild(elementIdx)  // TBD only call this if necessary!
                    if (!type.canConsumeMore()) {
                        typeIdx++
                    }
                    elementIdx++
                } else {
                    if (!type.canConsumeMore()) {
                        typeIdx++
                        if (!type.isValidCountWithinRange()) {
                            elementIdx++
                            violation.add(elementValidation)
                        }
                    } else if (type.attemptsSatisfyOccurrences()) {
                        typeIdx++
                        type.validateValidCount(issues)
                        violation.add(elementValidation)
                    }
                }
            }
        }
    }
}

