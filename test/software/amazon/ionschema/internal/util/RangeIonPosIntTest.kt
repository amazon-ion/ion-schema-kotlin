package software.amazon.ionschema.internal.util

import org.junit.Assert.fail
import org.junit.Test
import software.amazon.ionschema.internal.ION
import software.amazon.ionschema.InvalidSchemaException

class RangeIonPosIntTest {
    @Test
    fun invalidRanges() {
        testInvalidRange("[exclusive::1, 1]")
        testInvalidRange("[1, exclusive::1]")

        testInvalidRange("[0, exclusive::1]")
        testInvalidRange("[exclusive::0, 1]")
    }

    private fun testInvalidRange(rangeDef: String) {
        try {
            Range.rangeOf(ION.singleValue(rangeDef), Range.RangeType.POSITIVE_INTEGER)
            fail("Expected InvalidSchemaException for RangeIonPosInt($rangeDef)")
        } catch (e: InvalidSchemaException) {
        }
    }
}