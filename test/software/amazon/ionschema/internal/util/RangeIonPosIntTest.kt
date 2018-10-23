package software.amazon.ionschema.internal.util

import org.junit.Assert.fail
import org.junit.Test
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.InvalidSchemaException

class RangeIonPosIntTest {
    private val ION = IonSystemBuilder.standard().build()

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

