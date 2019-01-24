package software.amazon.ionschema

interface IonSchemaSystem {
    fun loadSchema(id: String): Schema
}

