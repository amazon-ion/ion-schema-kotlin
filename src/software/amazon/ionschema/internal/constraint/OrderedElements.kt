package software.amazon.ionschema.internal.constraint

import software.amazon.ion.*
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.Occurs.Companion.REQUIRED
import software.amazon.ionschema.ViolationChild
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.CommonViolations

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

        var elementValidation = newViolationChild(elementIdx, elements)

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
                    elementValidation = newViolationChild(elementIdx, elements)
                    elementValidation.add(
                            Violation(type.occursIon,
                                    "occurs_mismatch",
                                    "expected %s occurrences, found %s".format(type.range, type.validCount)))

                    violation.add(elementValidation)
                }

                // keep going...there may be other types that expect to be matched
                typeIdx++

            } else {
                val checkpoint = elementValidation.checkpoint()
                type.validate(elements[elementIdx], elementValidation)
                if (checkpoint.isValid()) {
                    if (!type.canConsumeMore()) {
                        typeIdx++
                    }
                    elementIdx++
                    elementValidation = newViolationChild(elementIdx, elements)
                } else {
                    if (!type.canConsumeMore()) {
                        typeIdx++
                        if (!type.range.contains(0)) {
                            violation.add(elementValidation)
                            elementIdx++
                            elementValidation = newViolationChild(elementIdx, elements)
                        }
                    } else if (type.attemptsSatisfyOccurrences()) {
                        typeIdx++
                        type.validateValidCount(issues)
                    }
                }
            }
        }
    }

    private fun newViolationChild(idx: Int, elements: IonSequence) =
        if (idx < elements.size) {
            ViolationChild(index = idx, value = elements[idx])
        } else {
            ViolationChild(index = idx)
        }
}

