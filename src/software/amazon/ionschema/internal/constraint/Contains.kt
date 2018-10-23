package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonContainer
import software.amazon.ion.IonList
import software.amazon.ion.IonValue

internal class Contains(
        ion: IonValue
    ) : ConstraintBase(ion) {

    private val expectedElements = (ion as IonList).toArray()

    override fun isValid(value: IonValue): Boolean {
        if (value is IonContainer) {
            val expectedValues = expectedElements.toMutableSet()
            value.forEach {
                expectedValues.remove(it)
            }
            return expectedValues.isEmpty()
        }
        return false
    }
}
