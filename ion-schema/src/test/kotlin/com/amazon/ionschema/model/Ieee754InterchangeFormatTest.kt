package com.amazon.ionschema.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class Ieee754InterchangeFormatTest {

    @ParameterizedTest(name = "symbolText for {0}")
    @EnumSource
    fun `test symbolText`(iif: Ieee754InterchangeFormat) {
        val expected = when (iif) {
            Ieee754InterchangeFormat.Binary16 -> "binary16"
            Ieee754InterchangeFormat.Binary32 -> "binary32"
            Ieee754InterchangeFormat.Binary64 -> "binary64"
        }
        Assertions.assertEquals(expected, iif.symbolText)
    }

    @ParameterizedTest(name = "fromSymbolTextOrNull for {0}")
    @EnumSource
    fun `test fromSymbolTextOrNull`(iif: Ieee754InterchangeFormat) {
        val symbolText = iif.symbolText
        Assertions.assertEquals(iif, Ieee754InterchangeFormat.fromSymbolTextOrNull(symbolText))
    }

    @Test
    fun `fromSymbolTextOrNull should return null when not a valid timestamp precision value`() {
        Assertions.assertNull(Ieee754InterchangeFormat.fromSymbolTextOrNull("unary42"))
    }

    @ParameterizedTest(name = "fromSymbolText for {0}")
    @EnumSource
    fun `test fromSymbolText`(iif: Ieee754InterchangeFormat) {
        val symbolText = iif.symbolText
        Assertions.assertEquals(iif, Ieee754InterchangeFormat.fromSymbolText(symbolText))
    }

    @Test
    fun `fromSymbolTextOrNull should throw exception when not a valid timestamp precision value`() {
        assertThrows<IllegalArgumentException> { Ieee754InterchangeFormat.fromSymbolText("unary42") }
    }
}
