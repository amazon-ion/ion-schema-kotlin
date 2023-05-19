package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.VariablyOccurringTypeArgument
import com.amazon.ionschema.model.VariablyOccurringTypeArgument.Companion.OCCURS_REQUIRED
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalIonSchemaModel::class)
class OrderedElementsReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'ordered_elements'`() {
        assertTrue(OrderedElementsReader(mockk()).canRead("ordered_elements"))
    }

    @Test
    fun `canRead should return false for any field other than 'ordered_elements'`() {
        val reader = OrderedElementsReader(mockk())
        Assertions.assertFalse(reader.canRead("randomized_elements"))
    }

    @Test
    fun `reading a field that is not a supported field should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = OrderedElementsReader(mockk())
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reading a valid ordered_elements constraint should return an OrderedElements instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = OrderedElementsReader(typeReader)
        val mockTypeString = mockk<VariablyOccurringTypeArgument>()
        val mockTypeInt = mockk<VariablyOccurringTypeArgument>()
        val context = ReaderContext()

        val struct = ION.singleValue("""{ ordered_elements: [ string, int ] }""") as IonStruct

        every { typeReader.readVariablyOccurringTypeArg(context, ION.newSymbol("string"), OCCURS_REQUIRED) } returns mockTypeString
        every { typeReader.readVariablyOccurringTypeArg(context, ION.newSymbol("int"), OCCURS_REQUIRED) } returns mockTypeInt

        val expected = Constraint.OrderedElements(listOf(mockTypeString, mockTypeInt))

        val actual = reader.readConstraint(context, struct["ordered_elements"])
        assertEquals(expected, actual)
    }
}
