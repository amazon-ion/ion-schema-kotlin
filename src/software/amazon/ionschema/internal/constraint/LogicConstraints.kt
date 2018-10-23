package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonList
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema

internal abstract class LogicConstraints(
        ion: IonValue,
        schema: Schema
    ) : ConstraintBase(ion) {

    internal val types = (ion as IonList).map { TypeReference(it, schema) }

    internal fun getCount(value: IonValue): Int {
        var count = 0
        types.forEach {
            if (it.isValid(value)) {
                count++
            }
        }
        return count
    }
}

internal class AllOf(ion: IonValue, schema: Schema) : LogicConstraints(ion, schema) {
    override fun isValid(value: IonValue) = getCount(value) == types.size
}

internal class AnyOf(ion: IonValue, schema: Schema) : LogicConstraints(ion, schema) {
    override fun isValid(value: IonValue) = getCount(value) >= 1
}

internal class OneOf(ion: IonValue, schema: Schema) : LogicConstraints(ion, schema) {
    override fun isValid(value: IonValue) = getCount(value) == 1
}

internal class Not(ion: IonValue, schema:Schema) : ConstraintBase(ion) {
    private val type = TypeReference(ion, schema)
    override fun isValid(value: IonValue) = !type.isValid(value)
}
