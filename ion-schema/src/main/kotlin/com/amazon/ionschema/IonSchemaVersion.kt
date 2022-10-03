package com.amazon.ionschema

import com.amazon.ion.IonSymbol

enum class IonSchemaVersion {
    v1_0,
    v2_0;

    val directoryName = "ion_schema_${name.drop(1)}"
    val symbolText = "$$directoryName"

    companion object {
        @JvmStatic
        fun fromIonSymbol(symbol: IonSymbol): IonSchemaVersion = fromVersionMarkerString(symbol.stringValue())

        @JvmStatic
        fun fromVersionMarkerString(islvm: String) = valueOf('v' + islvm.substringAfter("\$ion_schema"))
    }
}
