package software.amazon.ionschema.internal.util

import org.junit.Test
import software.amazon.ion.IonList
import software.amazon.ion.IonValue

internal class RangeIonNumberTest
    : AbstractRangeTest(RangeType.ION_NUMBER)
{
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> rangeOf(ion: IonList)
            = RangeFactory.rangeOf<IonValue>(ion, RangeType.ION_NUMBER) as Range<T>

    @Test
    fun range_decimal_inclusive() {
        assertValidRangeAndValues(
                "range::[-100d0, 100d0]",
                ionListOf("-100", "-100d0", "-100.00000000d0", "-100e0", "-100.00000000e0",
                           "100",  "100d0",  "100.00000000d0",  "100e0", "100.00000000e0"),

                ionListOf("-101", "-100.00000001d0", "-100.00000001e0",
                           "101",  "100.00000001d0",  "100.00000001e0"))
    }

    @Test
    fun range_decimal_exclusive() {
        assertValidRangeAndValues(
                "range::[exclusive::-100d0, exclusive::100d0]",
                ionListOf("-99", "-99.99999999d0", "-99.99999999e0",
                           "99",  "99.99999999d0",  "99.99999999e0"),

                ionListOf("-100", "-100d0", "-100.00000000d0", "-100e0", "-100.00000000e0",
                           "100",  "100d0",  "100.00000000d0",  "100e0",  "100.00000000e0"))
    }

    @Test
    fun range_float_inclusive() {
        assertValidRangeAndValues(
                "range::[-100e0, 100e0]",
                ionListOf("-100", "-100d0", "-100.00000000d0", "-100e0", "-100.00000000e0",
                           "100",  "100d0",  "100.00000000d0",  "100e0",  "100.00000000e0"),

                ionListOf("-101", "-100.00000001d0", "-100.00000001e0",
                           "101",  "100.00000001d0",  "100.00000001e0"))
    }

    @Test
    fun range_float_exclusive() {
        assertValidRangeAndValues(
                "range::[exclusive::-100e0, exclusive::100e0]",
                ionListOf("-99", "-99.99999999d0", "-99.99999999e0",
                           "99",  "99.99999999d0",  "99.99999999e0"),

                ionListOf("-100", "-100d0", "-100.00000000d0", "-100e0", "-100.00000000e0",
                           "100",  "100d0",  "100.00000000d0",  "100e0",  "100.00000000e0"))
    }

    @Test
    fun range_int_inclusive() {
        assertValidRangeAndValues(
                "range::[-100, 100]",
                ionListOf("-100", "0", "100"),
                ionListOf("-101", "101"))
    }

    @Test
    fun range_int_exclusive() {
        assertValidRangeAndValues(
                "range::[exclusive::-100,exclusive::100]",
                ionListOf("-99", "0", "99"),
                ionListOf("-100", "100"))
    }

    @Test
    fun range_invalid() {
        assertInvalidRange("range::[1, min]")
        assertInvalidRange("range::[max, 1]")
        assertInvalidRange("range::[max, min]")

        assertInvalidRange("range::[exclusive::1, 1]")
        assertInvalidRange("range::[1, exclusive::1]")

        assertInvalidRange("range::[exclusive::2d0, 2d0]")
        assertInvalidRange("range::[2d0, exclusive::2d0]")

        assertInvalidRange("range::[exclusive::3e0, 3e0]")
        assertInvalidRange("range::[3e0, exclusive::3e0]")

        assertInvalidRange("range::[1, 0]")
        assertInvalidRange("range::[1.00000000d0, 0.99999999d0]")
        assertInvalidRange("range::[1.00000000e0, 0.99999999e0]")
    }
}

