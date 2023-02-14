package com.amazon.ionschema

import com.amazon.ion.system.IonSystemBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

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

    @ParameterizedTest
    @ValueSource(
        strings = [
            "\$ion_schema_1_0",
            "\$ion_schema_2_0",
            "\$ion_schema_0_1",
            "\$ion_schema_0_1_2_3",
            "\$ion_schema_0_abc",
            "\$ion_schema_0",
        ]
    )
    fun `isVersionMarker() should recognize symbols that are reserved for version markers`(ion: String) {
        assertTrue(IonSchemaVersion.isVersionMarker(ION.singleValue(ion)))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "\$ion_schema_a",
            "\$ion_schema",
            "\"\$ion_schema_1_0\"",
            "null.symbol",
            "\$ion_schema_1_0::null.symbol"
        ]
    )
    fun `isVersionMarker() should reject values that are not reserved for version markers`(ion: String) {
        assertFalse(IonSchemaVersion.isVersionMarker(ION.singleValue(ion)))
    }
}
