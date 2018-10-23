package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema

internal class Element(
        ion: IonValue,
        schema: Schema
    ) : ConstraintBase(ion) {

    private val typeReference = TypeReference(ion, schema)

    override fun isValid(value: IonValue): Boolean {
        if (value is IonContainer) {
            value.forEach {
                if (!typeReference.isValid(it)) {
                    return false
                }
            }
            return true
        }
        return false
    }
}
