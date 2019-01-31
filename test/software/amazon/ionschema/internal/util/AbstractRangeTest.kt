package software.amazon.ionschema.internal.util

import org.junit.Assert
import org.junit.Assert.fail
import software.amazon.ion.IonList
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.InvalidSchemaException

internal abstract class AbstractRangeTest(
        private val rangeType: RangeType
) {
    private val ION = IonSystemBuilder.standard().build()

    abstract fun <T : Any> rangeOf(ion: IonList): Range<T>

    fun assertValidRangeAndValues(
            rangeDef: String,
            validValues: List<Any>,
            invalidValues: List<Any>) {

        val range = rangeOf<Any>(ION.singleValue(rangeDef) as IonList)
        validValues.forEach {
            Assert.assertTrue("Expected $it to be within $rangeDef",
                    range.contains(it))
        }
        invalidValues.forEach {
            Assert.assertFalse("Didn't expect $it to be within $rangeDef",
                    range.contains(it))
        }
    }

    fun assertInvalidRange(rangeDef: String) {
        try {
            rangeOf<Any>(ION.singleValue(rangeDef) as IonList)
            fail("Expected InvalidSchemaException for $rangeDef")
        } catch (e: InvalidSchemaException) {
        }
    }

    fun ionListOf(vararg items: String): IonList {
        val ionList = ION.newEmptyList()
        items.forEach {
            ionList.add(ION.singleValue(it))
        }
        return ionList
    }
}

