package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.util.Range

internal class RecurringTypeReference(
        ion: IonValue,
        schema: Schema,
        defaultRange: Range
    ) : ConstraintBase(ion) {

    companion object {
        private val ION = IonSystemBuilder.standard().build()

        internal val OPTIONAL = Range.rangeOf(ION.singleValue("range::[0, 1]"),
                Range.RangeType.POSITIVE_INTEGER)
        internal val REQUIRED = Range.rangeOf(ION.singleValue("range::[1, 1]"),
                Range.RangeType.POSITIVE_INTEGER)
    }

    internal val range: Range
    private val typeReference: TypeReference
    internal var counter = 0
    private var validSummary: Boolean = true

    init {
        var occurs: IonValue? = null
        var tmpRange = defaultRange
        if (ion is IonStruct) {
            occurs = ion.get("occurs")
            if (occurs != null) {
                if (occurs is IonSymbol) {
                    when (occurs.stringValue()) {
                        "optional" -> tmpRange = OPTIONAL
                        "required" -> tmpRange = REQUIRED
                        else -> throw IllegalArgumentException("Unrecognized occurs constraint value '$occurs'")
                    }
                } else {
                    tmpRange = Range.rangeOf(occurs, Range.RangeType.POSITIVE_INTEGER)
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
        typeReference = TypeReference(tmpIon, schema)
    }

    override fun isValid(value: IonValue): Boolean {
        val valid = typeReference.isValid(value)
        if (valid) {
            counter++
        } else {
            validSummary = false
        }
        return valid
    }

    internal fun isValid() = validSummary && range.contains(counter)
}
