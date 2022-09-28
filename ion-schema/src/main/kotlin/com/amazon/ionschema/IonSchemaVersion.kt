package com.amazon.ionschema

import com.amazon.ion.IonSymbol
import com.amazon.ion.IonSystem

enum class IonSchemaVersion {
    ION_SCHEMA_1_0,
    ION_SCHEMA_2_0;

    fun toSymbolText() = '$' + name.toLowerCase()

    fun toDirectoryName() = name.toLowerCase()

    fun toIonSymbol(system: IonSystem) = system.newSymbol(toSymbolText())

    companion object {
        @JvmStatic
        fun fromIonSymbol(symbol: IonSymbol): IonSchemaVersion = fromVersionMarkerString(symbol.stringValue())

        @JvmStatic
        fun fromVersionMarkerString(islvm: String) = valueOf(islvm.toUpperCase().substringAfter('$'))
    }
}
