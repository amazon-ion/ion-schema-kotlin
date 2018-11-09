package software.amazon.ionschema.internal

import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.IonSchemaSystem

internal class TypeIon(
        private val name: String
    ) : TypeInternal {

    private val ionType = IonType.valueOf(name.toUpperCase().substring(1))

    override fun name() = name

    override fun isValid(value: IonValue) = ionType.equals(value.type)

    override fun isValidForBaseType(value: IonValue) = isValid(value)
}
