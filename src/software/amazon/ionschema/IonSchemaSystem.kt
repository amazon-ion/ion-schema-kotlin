package software.amazon.ionschema

import software.amazon.ion.IonSystem

interface IonSchemaSystem {
    fun loadSchema(id: String): Schema

    fun getIonSystem(): IonSystem
}
