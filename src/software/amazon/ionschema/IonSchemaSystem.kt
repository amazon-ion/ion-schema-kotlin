package software.amazon.ionschema

import software.amazon.ion.IonSystem
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.*
import java.io.Reader

interface IonSchemaSystem {
    fun loadSchema(id: String): Schema

    fun loadSchema(reader: Reader): Schema

    fun getIonSystem(): IonSystem
}
