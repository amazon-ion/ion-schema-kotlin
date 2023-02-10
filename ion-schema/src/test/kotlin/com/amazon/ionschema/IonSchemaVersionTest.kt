package com.amazon.ionschema

import com.amazon.ion.system.IonSystemBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class IonSchemaVersionTest {

    val ION = IonSystemBuilder.standard().build()

    @Test
    fun `fromIonSymbolOrNull should correctly match an ISL 1,0 version symbol`() {
        val islVersionSymbol = ION.newSymbol("\$ion_schema_1_0")
        assertEquals(IonSchemaVersion.v1_0, IonSchemaVersion.fromIonSymbolOrNull(islVersionSymbol))
    }

    @Test
    fun `fromIonSymbolOrNull should correctly match an ISL 2,0 version symbol`() {
        val islVersionSymbol = ION.newSymbol("\$ion_schema_2_0")
        assertEquals(IonSchemaVersion.v2_0, IonSchemaVersion.fromIonSymbolOrNull(islVersionSymbol))
    }

    @Test
    fun `fromIonSymbolOrNull should return null for any other symbol`() {
        val islVersionSymbol = ION.newSymbol("\$ion_schema_0_1")
        assertNull(IonSchemaVersion.fromIonSymbolOrNull(islVersionSymbol))
    }
}
