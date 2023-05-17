package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalIonSchemaModel
class ElementV2ReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'element'`() {
        val reader = ElementV2Reader(mockk())
        Assertions.assertTrue(reader.canRead("element"))
    }

    @Test
    fun `canRead should return false for any field other than 'element'`() {
        val reader = ElementV2Reader(mockk())
        Assertions.assertFalse(reader.canRead("elephant"))
    }

    @Test
    fun `reading a field that is not named 'element' should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = ElementV2Reader(mockk())
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reading a valid element constraint should return a Element instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = ElementV2Reader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ element: symbol }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.Element(mockType)
        val actual = reader.readConstraint(context, struct["element"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading a valid element constraint with 'distinct' should return a Element instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = ElementV2Reader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ element: distinct::symbol }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.Element(mockType, distinct = true)
        val actual = reader.readConstraint(context, struct["element"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading a valid element constraint with '$null_or' should return a ElementV2 instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = ElementV2Reader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ element: ${'$'}null_or::symbol }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.Element(mockType)
        val actual = reader.readConstraint(context, struct["element"])
        kotlin.test.assertEquals(expected, actual)
    }

    @Test
    fun `reading a element constraint with any annotation other than 'distinct' or '$null_or' should throw exception`() {
        val typeReader = mockk<TypeReader>()
        val reader = ElementV2Reader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ element: foo::symbol }""") as IonStruct
        val context = ReaderContext()

        assertThrows<InvalidSchemaException> {
            reader.readConstraint(context, struct["element"])
        }
    }
}
