package software.amazon.ionschema.internal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.IonSchemaException
import software.amazon.ionschema.internal.util.RangeBoundaryType.INCLUSIVE
import software.amazon.ionschema.internal.util.RangeBoundaryType.EXCLUSIVE

internal class RangeBoundaryTypeTest {
    private val ION = IonSystemBuilder.standard().build()

    @Test fun exclusive()     { assert(EXCLUSIVE, "exclusive::5") }
    @Test fun inclusive()     { assert(INCLUSIVE, "min") }
    @Test fun max()           { assert(INCLUSIVE, "max") }
    @Test fun min()           { assert(INCLUSIVE, "min") }
    @Test fun max_exclusive() { assertException("exclusive::max") }
    @Test fun min_exclusive() { assertException("exclusive::min") }

    private fun assert(expected: RangeBoundaryType, str: String) {
        assertEquals(expected, RangeBoundaryType.forIon(ION.singleValue(str)))
    }

    private fun assertException(str: String) {
        try {
            RangeBoundaryType.forIon(ION.singleValue(str))
            fail("Expected an IonSchemaException")
        } catch (e: IonSchemaException) {
        }
    }
}

