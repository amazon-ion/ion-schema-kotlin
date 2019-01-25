package software.amazon.ionschema.internal.util

import org.junit.Assert
import org.junit.Assert.fail
import software.amazon.ion.IonList
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.InvalidSchemaException

internal abstract class AbstractRangeTest(
        private val rangeType: Range.RangeType
) {
    private val ION = IonSystemBuilder.standard().build()

    fun assertValidRangeAndValues(
            rangeDef: String,
            validValues: List<String>,
            invalidValues: List<String>) {

        val range = Range.rangeOf(ION.singleValue(rangeDef), rangeType)
        validValues.forEach {
            Assert.assertTrue("Expected $it to be within $rangeDef",
                    range.contains(ION.singleValue(it)))
        }
        invalidValues.forEach {
            Assert.assertFalse("Didn't expect $it to be within $rangeDef",
                    range.contains(ION.singleValue(it)))
        }
    }

    fun assertInvalidRange(rangeDef: String) {
        try {
            Range.rangeOf(ION.singleValue(rangeDef) as IonList, rangeType)
            fail("Expected InvalidSchemaException for RangeIonNumber($rangeDef)")
        } catch (e: InvalidSchemaException) {
        }
    }
}

