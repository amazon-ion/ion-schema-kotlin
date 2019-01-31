package software.amazon.ionschema.internal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import software.amazon.ion.IonTimestamp
import software.amazon.ion.system.IonSystemBuilder

class IonTimestampPrecisionTest {
    private val ION = IonSystemBuilder.standard().build()

    @Test
    fun timestamp_precisions_valid() {
        assert(IonTimestampPrecision.year.id,        "2000T")
        assert(IonTimestampPrecision.month.id,       "2000-01T")
        assert(IonTimestampPrecision.day.id,         "2000-01-01T")
        // hour without minute is not valid Ion
        assert(IonTimestampPrecision.minute.id,      "2000-01-01T00:00Z")
        assert(IonTimestampPrecision.second.id,      "2000-01-01T00:00:00Z")
        assert(1,                                    "2000-01-01T00:00:00.0Z")
        assert(2,                                    "2000-01-01T00:00:00.00Z")
        assert(IonTimestampPrecision.millisecond.id, "2000-01-01T00:00:00.000Z")
        assert(4,                                    "2000-01-01T00:00:00.0000Z")
        assert(5,                                    "2000-01-01T00:00:00.00000Z")
        assert(IonTimestampPrecision.microsecond.id, "2000-01-01T00:00:00.000000Z")
        assert(7,                                    "2000-01-01T00:00:00.0000000Z")
        assert(8,                                    "2000-01-01T00:00:00.00000000Z")
        assert(IonTimestampPrecision.nanosecond.id,  "2000-01-01T00:00:00.000000000Z")
    }

    @Test
    fun timestamp_precisions_invalid() {
        assertInvalid("2000-01-01T00Z")
    }

    private fun assert(expected: Int, value: String) {
        assertEquals(expected,
                IonTimestampPrecision.toInt(ION.singleValue(value) as IonTimestamp))
    }

    private fun assertInvalid(value: String) {
        try {
            IonTimestampPrecision.toInt(ION.singleValue(value) as IonTimestamp)
            fail("Expected an exception from $value")
        } catch (e: RuntimeException) {
        }
    }
}

