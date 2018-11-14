package software.amazon.ionschema.internal.constraint

import software.amazon.ion.*
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.RecurringTypeReference.Companion.REQUIRED

internal class OrderedElements(
        private val ion: IonValue,
        private val schema: Schema
    ) : ConstraintBase(ion) {

    init {
        (ion as IonList).map { RecurringTypeReference(it, schema, REQUIRED) }
    }

    // TBD this impl does not backtrack
    override fun isValid(value: IonValue): Boolean {
        val types = (ion as IonList).map {
            RecurringTypeReference(it, schema, REQUIRED)
        }.toList()

        if (value is IonSequence && !value.isNullValue) {
            val values = value
            var valueIndex = 0
            var typeIndex = 0

            while (true) {
                if (typeIndex == types.size) {
                    // we've exhausted the types, have we exhausted the values?
                    return valueIndex == values.size
                }

                var type = types.get(typeIndex)

                if (valueIndex == values.size) {
                    // we've exhausted the values, did we meet the type's "occurs" requirements?
                    if (!type.range.contains(type.counter)) {
                        return false
                    }

                    // keep going...there may be other types that expect to be matched
                    typeIndex++
                    continue
                }

                var curValue = values[valueIndex]

                if (type.isValid(curValue)) {
                    valueIndex++
                } else {
                    if (!type.range.contains(type.counter)) {
                        // the type's "occurs" requirement wasn't achieved
                        return false
                    }
                    typeIndex++
                    continue
                }

                if (type.range.compareTo(type.counter + 1) > 0) {
                    typeIndex++
                }
            }
        }
        return false
    }
}
