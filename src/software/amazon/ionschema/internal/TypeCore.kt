package software.amazon.ionschema.internal

import software.amazon.ion.IonType
import software.amazon.ion.IonValue

internal class TypeCore(name: String) : TypeInternal {
    private val typeName = ION.newSymbol(name)
    private val ionType = IonType.valueOf(name.toUpperCase())

    override fun name() = typeName

    override fun isValid(value: IonValue) =
            !value.isNullValue
            && ionType.equals(value.type)

    override fun isValidForBaseType(value: IonValue) = ionType.equals(value.type)
}
