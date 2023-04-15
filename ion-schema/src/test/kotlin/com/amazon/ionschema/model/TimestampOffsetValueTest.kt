package com.amazon.ionschema.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimestampOffsetValueTest {

    fun validOffsets(): Iterable<Arguments> = listOf(
        //  1. int minutes or null (unknown offset) 2. string
        arguments(null, "-00:00"),
        arguments(0, "+00:00"),
        arguments(1, "+00:01"),
        arguments(59, "+00:59"),
        arguments(60, "+01:00"),
        arguments(1439, "+23:59"),
        arguments(-1, "-00:01"),
        arguments(-59, "-00:59"),
        arguments(-60, "-01:00"),
        arguments(-1439, "-23:59"),
    )

    @ParameterizedTest(name = "parse(\"{1}\") should be an offset of {0} minutes")
    @MethodSource("validOffsets")
    fun `parse should handle valid timestamp offset strings`(expectedMinutes: Int?, stringValue: String) {
        val expectedOffset = TimestampOffsetValue.fromMinutes(expectedMinutes)
        assertEquals(expectedOffset, TimestampOffsetValue.parse(stringValue))
    }

    @ParameterizedTest(name = "TimestampOffsetValue.fromMinutes({0}).toString() should be \"{1}\"")
    @MethodSource("validOffsets")
    fun `toString() should produce the correct timestamp offset string`(minutes: Int?, expected: String) {
        val offset = TimestampOffsetValue.fromMinutes(minutes)
        assertEquals(expected, offset.toString())
    }

    @ParameterizedTest(name = "constructor should throw exception for {0} minutes")
    @ValueSource(ints = [1440, -1440])
    fun `constructor should throw exception for invalid number of minutes`(minutes: Int) {
        assertThrows<IllegalArgumentException> { TimestampOffsetValue.Known(minutes) }
    }

    @ParameterizedTest(name = "parse(\"{0}\") should throw exception")
    @ValueSource(
        strings = [
            "00:00", // no sign
            "*00:00", // sign is not + or -
            "+1:00", // missing zero-padding on hours
            "+01:1", // missing zero-padding on minutes
            "+001:00", // extra zero-padding on hours
            "+01:001", // extra zero-padding on minutes
            "+00:60", // minutes too high
            "-00:60", // minutes too low
            "+24:00", // hours too high
            "-24:00", // hours too low
            "+0000", // No ':' separator
        ]
    )
    fun `parse() should throw exception for invalid offset string`(string: String) {
        assertThrows<IllegalArgumentException> { TimestampOffsetValue.parse(string) }
    }
}
