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
import software.amazon.ionschema.internal.TypeInternal
import software.amazon.ionschema.internal.TypeReference
import software.amazon.ionschema.internal.constraint.Occurs.Companion.toRange
import software.amazon.ionschema.internal.util.Range
import software.amazon.ionschema.internal.util.RangeFactory
import software.amazon.ionschema.internal.util.RangeIntNonNegative
import software.amazon.ionschema.internal.util.RangeType

/**
 * Implements the occurs constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#occurs
 */
internal open class Occurs(
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

        private val OPTIONAL_ION = (ION.singleValue("{ occurs: optional }") as IonStruct).get("occurs")
        private val REQUIRED_ION = (ION.singleValue("{ occurs: required }") as IonStruct).get("occurs")

        internal fun toRange(ion: IonValue): Range<Int> {
            if (!ion.isNullValue) {
                return if (ion is IonSymbol) {
                           when (ion) {
                               OPTIONAL_ION -> OPTIONAL
                               REQUIRED_ION -> REQUIRED
                               else -> throw InvalidSchemaException("Invalid ion constraint '$ion'")
                           }
                       } else {
                           val range = RangeFactory.rangeOf<Int>(ion, RangeType.INT_NON_NEGATIVE)
                           if (range.contains(0) && !range.contains(1)) {
                               throw InvalidSchemaException("Occurs must allow at least one value ($ion)")
                           }
                           range
                       }
            }
            throw InvalidSchemaException("Invalid occurs constraint '$ion'")
        }
    }

    internal val range: Range<Int>
    internal val occursIon: IonValue
    private val typeReference: () -> TypeInternal
    private var attempts = 0
    internal var validCount = 0
    private val values = ion.system.newEmptyList()

    init {
        var occurs: IonValue? = null
        range =
            if (ion is IonStruct && !ion.isNullValue) {
                occurs = ion["occurs"]
                if (occurs != null) {
                    toRange(occurs)
                } else {
                    defaultRange
                }
            } else {
                defaultRange
            }

        val tmpIon = if (ion is IonStruct && occurs != null) {
                ion.cloneAndRemove("occurs")
            } else {
                ion
            }
        typeReference = TypeReference.create(tmpIon, schema, isField)

        occursIon =
            if (occurs != null) {
                occurs
            } else {
                when (range) {
                    OPTIONAL -> OPTIONAL_ION
                    REQUIRED -> REQUIRED_ION
                    else -> throw InvalidSchemaException("Invalid occurs constraint '$range'")
                }
            }
    }

    override fun validate(value: IonValue, issues: Violations) {
        attempts++

        typeReference().validate(value, issues)
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

/**
 * This class should only be used during load/validation of a type definition.
 * The real Occurs constraint implementation is instantiated and used for validation
 * by the Fields and OrderedElements constraints.
 */
internal open class OccursNoop(
        ion: IonValue
) : ConstraintBase(ion) {

    init {
        toRange(ion)
    }

    override fun validate(value: IonValue, issues: Violations) {
        // no-op
    }
}

