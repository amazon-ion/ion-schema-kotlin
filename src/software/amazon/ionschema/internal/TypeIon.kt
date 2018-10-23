package software.amazon.ionschema.internal

import software.amazon.ion.IonType
import software.amazon.ion.IonValue

internal class TypeIon(name: String) : TypeInternal {
    private val typeName = ION.newSymbol(name)
    private val ionType = IonType.valueOf(name.toUpperCase().substring(1))

    override fun name() = typeName

    override fun isValid(value: IonValue) = ionType.equals(value.type)

    override fun isValidForBaseType(value: IonValue) = isValid(value)
}
