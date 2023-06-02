package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExperimentalIonSchemaModel
class LogicConstraintsReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @ValueSource(strings = ["all_of", "any_of", "not", "one_of", "type"])
    @ParameterizedTest(name = "canRead should return true for {0}")
    fun `canRead should return true for supported constraints`(fieldName: String) {
        assertTrue(LogicConstraintsReader(mockk()).canRead(fieldName))
    }

    @Test
    fun `canRead should return false for unsupported constraints`() {
        val reader = LogicConstraintsReader(mockk())
        Assertions.assertFalse(reader.canRead("two_of"))
    }

    @Test
    fun `reading a field that is not a supported constraint should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = LogicConstraintsReader(mockk())
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reading a single-arg logic constraint should return a constraint instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = LogicConstraintsReader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ type: symbol }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.Type(mockType)
        val actual = reader.readConstraint(context, struct["type"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading a multi-arg logic constraint should return a constraint instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = LogicConstraintsReader(typeReader)
        val mockType1 = mockk<TypeArgument>()
        val mockType2 = mockk<TypeArgument>()

        every { typeReader.readTypeArgumentList(any(), any()) } returns setOf(mockType1, mockType2)

        val struct = ION.singleValue("""{ any_of: [symbol, string] }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.AnyOf(setOf(mockType1, mockType2))
        val actual = reader.readConstraint(context, struct["any_of"])
        assertEquals(expected, actual)
    }
}
