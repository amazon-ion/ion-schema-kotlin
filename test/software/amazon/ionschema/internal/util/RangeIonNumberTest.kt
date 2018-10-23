package software.amazon.ionschema.internal.util

import org.junit.Assert.fail
import org.junit.Test
import software.amazon.ion.IonList
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.internal.util.Range

class RangeIonNumberTest {
    private val ION = IonSystemBuilder.standard().build()

    /*
    @Test
    fun range() {
        testRange("[-100,100]",
                listOf("-100", "0", "100"),
                listOf("-101", "101"))
        testRange("[exclusive::-100,exclusive::100]",
                listOf("-99", "0", "99"),
                listOf("-100", "100"))
    }

    private fun testRange(rangeDef: String, inside: List<String>, outside: List<String>) {
        val range = RangeIonNumber.rangeOf(ION.singleValue(rangeDef))
        inside.forEach {
            assertTrue("Expected ${it} to be within $rangeDef",
                    range.contains(ION.singleValue(it)))
        }
        outside.forEach {
            assertFalse("Didn't expect ${it} to be within $rangeDef",
                    range.contains(ION.singleValue(it)))
        }
    }
    */

    @Test
    fun invalidRanges() {
        testInvalidRange("range::[1, min]")
        testInvalidRange("range::[max, 1]")
        testInvalidRange("range::[max, min]")

        testInvalidRange("range::[exclusive::1, 1]")
        testInvalidRange("range::[1, exclusive::1]")

        testInvalidRange("range::[exclusive::2d0, 2d0]")
        testInvalidRange("range::[2d0, exclusive::2d0]")

        testInvalidRange("range::[exclusive::3e0, 3e0]")
        testInvalidRange("range::[3e0, exclusive::3e0]")

        testInvalidRange("range::[1, 0]")
        testInvalidRange("range::[1.00000000d0, 0.99999999d0]")
        testInvalidRange("range::[1.00000000e0, 0.99999999e0]")
    }

    private fun testInvalidRange(rangeDef: String) {
        try {
            Range.rangeOf(ION.singleValue(rangeDef) as IonList, Range.RangeType.NUMBER)
            fail("Expected InvalidSchemaException for RangeIonNumber($rangeDef)")
        } catch (e: InvalidSchemaException) {
        }
    }
}

