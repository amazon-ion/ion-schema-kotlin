package com.amazon.ionschema.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class TimestampPrecisionValueTest {

    @ParameterizedTest(name = "symbolText for {0}")
    @EnumSource
    fun `test symbolText`(tpv: TimestampPrecisionValue) {
        val expected = when (tpv) {
            TimestampPrecisionValue.Year -> "year"
            TimestampPrecisionValue.Month -> "month"
            TimestampPrecisionValue.Day -> "day"
            TimestampPrecisionValue.Minute -> "minute"
            TimestampPrecisionValue.Second -> "second"
            TimestampPrecisionValue.Millisecond -> "millisecond"
            TimestampPrecisionValue.Microsecond -> "microsecond"
            TimestampPrecisionValue.Nanosecond -> "nanosecond"
        }
        assertEquals(expected, tpv.symbolText)
    }

    @ParameterizedTest(name = "fromSymbolTextOrNull for {0}")
    @EnumSource
    fun `test fromSymbolTextOrNull`(tpv: TimestampPrecisionValue) {
        val symbolText = tpv.symbolText
        assertEquals(tpv, TimestampPrecisionValue.fromSymbolTextOrNull(symbolText))
    }

    @Test
    fun `fromSymbolTextOrNull should return null when not a valid timestamp precision value`() {
        assertNull(TimestampPrecisionValue.fromSymbolTextOrNull("stardate"))
    }

    @ParameterizedTest(name = "fromSymbolText for {0}")
    @EnumSource
    fun `test fromSymbolText`(tpv: TimestampPrecisionValue) {
        val symbolText = tpv.symbolText
        assertEquals(tpv, TimestampPrecisionValue.fromSymbolText(symbolText))
    }

    @Test
    fun `fromSymbolTextOrNull should throw exception when not a valid timestamp precision value`() {
        assertThrows<IllegalArgumentException> { TimestampPrecisionValue.fromSymbolText("stardate") }
    }
}
