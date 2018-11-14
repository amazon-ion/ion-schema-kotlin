package software.amazon.ionschema

import software.amazon.ion.IonSymbol

interface Schema {
    fun getType(name: String): Type?

    fun getType(name: IonSymbol): Type?

    fun getTypes(): Iterator<Type>

    fun getSchemaSystem(): IonSchemaSystem
}
