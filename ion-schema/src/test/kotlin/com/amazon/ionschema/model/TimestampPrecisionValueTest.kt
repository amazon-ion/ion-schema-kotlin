package com.amazon.ionschema.model

import com.amazon.ion.Timestamp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class TimestampPrecisionValueTest {

    @ParameterizedTest(name = "symbolText for {0}")
    @MethodSource("com.amazon.ionschema.model.TimestampPrecisionValue#values")
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
            else -> null
        }
        assertEquals(expected, tpv.toSymbolTextOrNull())
    }

    @ParameterizedTest(name = "fromSymbolTextOrNull for {0}")
    @MethodSource("com.amazon.ionschema.model.TimestampPrecisionValue#values")
    fun `test fromSymbolTextOrNull`(tpv: TimestampPrecisionValue) {
        val symbolText = tpv.toSymbolTextOrNull()!!
        assertEquals(tpv, TimestampPrecisionValue.fromSymbolTextOrNull(symbolText))
    }

    @ParameterizedTest
    @CsvSource(
        "-4, 2023T",
        "-3, 2023-04T",
        "-2, 2023-04-05T",
        "-1, 2023-04-05T00:00Z",
        " 0, 2023-04-05T00:00:00Z",
        " 1, 2023-04-05T00:00:00.0Z",
        " 2, 2023-04-05T00:00:00.00Z",
        " 3, 2023-04-05T00:00:00.000Z",
        " 4, 2023-04-05T00:00:00.0000Z",
    )
    fun test(expectedIntValue: Int, timestamp: String) {
        val actual = TimestampPrecisionValue.fromTimestamp(Timestamp.valueOf(timestamp))
        assertEquals(expectedIntValue, actual.intValue)
    }

    @Test
    fun `fromSymbolTextOrNull should return null when not a valid timestamp precision value`() {
        assertNull(TimestampPrecisionValue.fromSymbolTextOrNull("stardate"))
    }

    @ParameterizedTest(name = "fromSymbolText for {0}")
    @MethodSource("com.amazon.ionschema.model.TimestampPrecisionValue#values")
    fun `test fromSymbolText`(tpv: TimestampPrecisionValue) {
        val symbolText = tpv.toSymbolTextOrNull()!!
        assertEquals(tpv, TimestampPrecisionValue.fromSymbolTextOrNull(symbolText))
    }
}
