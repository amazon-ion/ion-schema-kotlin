package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalIonSchemaModel
class ContainsReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'contains'`() {
        val reader = ContainsReader()
        Assertions.assertTrue(reader.canRead("contains"))
    }

    @Test
    fun `canRead should return false for any field other than 'contains'`() {
        val reader = ContainsReader()
        Assertions.assertFalse(reader.canRead("pertains"))
    }

    @Test
    fun `reading a field that is not named 'contains' should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = ContainsReader()
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `read contains with some values should return contains constraint`() {
        val struct = ION.singleValue("""{ contains: [a, 1, true] }""") as IonStruct
        val reader = ContainsReader()
        val context = ReaderContext()
        val actual = reader.readConstraint(context, struct["contains"])
        assertEquals(
            Constraint.Contains(
                setOf(
                    ION.newSymbol("a"),
                    ION.newInt(1),
                    ION.newBool(true),
                )
            ),
            actual
        )
    }
}
