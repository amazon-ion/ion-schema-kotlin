package software.amazon.ionschema

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue

interface Type {
    fun name(): IonSymbol
    fun isValid(value: IonValue): Boolean
}
