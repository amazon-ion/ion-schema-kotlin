package software.amazon.ionschema.internal

import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.IonSchemaSystem

internal class TypeCore(
        private val name: String
    ) : TypeInternal {

    private val ionType = IonType.valueOf(name.toUpperCase())

    override fun name() = name

    override fun isValid(value: IonValue) =
            !value.isNullValue
            && ionType.equals(value.type)

    override fun isValidForBaseType(value: IonValue) = ionType.equals(value.type)
}
