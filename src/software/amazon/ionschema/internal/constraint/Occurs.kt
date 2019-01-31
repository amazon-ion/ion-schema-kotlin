package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.ViolationChild
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.util.Range
import software.amazon.ionschema.internal.util.RangeFactory
import software.amazon.ionschema.internal.util.RangeIntNonNegative
import software.amazon.ionschema.internal.util.RangeType

internal class Occurs(
        ion: IonValue,
        schema: Schema,
        defaultRange: Range<Int>,
        isField: Boolean = false
    ) : ConstraintBase(ion) {

    companion object {
        private val ION = IonSystemBuilder.standard().build()

        internal val OPTIONAL = RangeFactory.rangeOf<Int>(ION.singleValue("range::[0, 1]"),
                RangeType.INT_NON_NEGATIVE)
        internal val REQUIRED = RangeFactory.rangeOf<Int>(ION.singleValue("range::[1, 1]"),
                RangeType.INT_NON_NEGATIVE)

        private val OPTIONAL_ION = (ION.singleValue("{ occurs: optional } ") as IonStruct).get("occurs")
        private val REQUIRED_ION = (ION.singleValue("{ occurs: required } ") as IonStruct).get("occurs")
    }

    internal val range: Range<Int>
    internal val occursIon: IonValue
    private val typeReference: TypeReference
    private var attempts = 0
    internal var validCount = 0
    private val values = ion.system.newEmptyList()

    init {
        var occurs: IonValue? = null
        var tmpRange = defaultRange
        if (ion is IonStruct) {
            occurs = ion.get("occurs")
            if (occurs != null) {
                tmpRange =
                    if (occurs is IonSymbol) {
                        when (occurs.stringValue()) {
                            "optional" -> OPTIONAL
                            "required" -> REQUIRED
                            else -> throw IllegalArgumentException("Unrecognized occurs constraint value '$occurs'")
                        }
                    } else {
                        RangeFactory.rangeOf<Int>(occurs, RangeType.INT_NON_NEGATIVE)
                    }
            }
        }
        range = tmpRange

        if (range.contains(0) && !range.contains(1)) {
            throw InvalidSchemaException("Occurs must allow at least one value ($ion)")
        }

        var tmpIon = ion
        if (ion is IonStruct && occurs != null) {
            tmpIon = ion.cloneAndRemove("occurs")
        }
        typeReference = TypeReference(tmpIon, schema, isField)

        occursIon = if (occurs != null) {
                occurs
            } else {
                when (range) {
                    OPTIONAL -> OPTIONAL_ION
                    REQUIRED -> REQUIRED_ION
                    else -> throw IllegalArgumentException("Unrecognized occurs constraint value '$range'")
                }
            }
    }

    override fun validate(value: IonValue, issues: Violations) {
        attempts++

        typeReference.validate(value, issues)
        validCount = attempts - issues.violations.size
        (issues as ViolationChild).addValue(value)

        values.add(value.clone())
    }

    fun validateAttempts(issues: Violations) {
        if (!range.contains(attempts)) {
            issues.add(Violation(occursIon, "occurs_mismatch",
                    "expected %s occurrences, found %s".format(range, attempts)))
        }
    }

    fun validateValidCount(issues: Violations) {
        if (!isValidCountWithinRange()) {
            issues.add(Violation(occursIon, "occurs_mismatch",
                    "expected %s occurrences, found %s".format(range, validCount)))
        }
    }

    internal fun isValidCountWithinRange() = range.contains(validCount)

    internal fun attemptsSatisfyOccurrences() = range.contains(attempts)
    internal fun canConsumeMore() = !(range as RangeIntNonNegative).isAtMax(attempts)
}

