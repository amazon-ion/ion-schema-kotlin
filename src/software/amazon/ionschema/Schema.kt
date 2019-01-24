package software.amazon.ionschema

interface Schema {
    fun getType(name: String): Type?

    fun getTypes(): Iterator<Type>

    fun getSchemaSystem(): IonSchemaSystem
}

