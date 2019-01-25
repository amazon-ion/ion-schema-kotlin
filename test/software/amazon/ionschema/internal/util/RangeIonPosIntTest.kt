package software.amazon.ionschema.internal.util

import org.junit.Test

internal class RangeIonPosIntTest
    : AbstractRangeTest(Range.RangeType.POSITIVE_INTEGER)
{
    @Test
    fun range_int_inclusive() {
        assertValidRangeAndValues(
                "range::[0, 100]",
                listOf("0", "100"),
                listOf("-1", "101"))
    }

    @Test
    fun range_int_exclusive() {
        assertValidRangeAndValues(
                "range::[exclusive::0, exclusive::100]",
                listOf("1", "99"),
                listOf("0", "100"))
    }

    @Test
    fun range_invalid() {
        assertInvalidRange("range::[-1, 0]")
        assertInvalidRange("range::[0, -1]")

        assertInvalidRange("range::[exclusive::1, 1]")
        assertInvalidRange("range::[1, exclusive::1]")

        assertInvalidRange("range::[0, exclusive::1]")
        assertInvalidRange("range::[exclusive::0, 1]")

        assertInvalidRange("range::[0d0, 1]")
        assertInvalidRange("range::[0, 1d0]")
        assertInvalidRange("range::[0e0, 1]")
        assertInvalidRange("range::[0, 1e0]")
    }
}

