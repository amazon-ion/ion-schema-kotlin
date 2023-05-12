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
class FieldNamesReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'field_names'`() {
        val reader = FieldNamesReader(mockk())
        Assertions.assertTrue(reader.canRead("field_names"))
    }

    @Test
    fun `canRead should return false for any field other than 'field_names'`() {
        val reader = FieldNamesReader(mockk())
        Assertions.assertFalse(reader.canRead("nield_fames"))
    }

    @Test
    fun `reading a field that is not named 'field_names' should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = FieldNamesReader(mockk())
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reading a valid field_names constraint should return a FieldNames instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = FieldNamesReader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ field_names: symbol }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.FieldNames(mockType)
        val actual = reader.readConstraint(context, struct["field_names"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading a valid field_names constraint with 'distinct' should return a FieldNames instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = FieldNamesReader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ field_names: distinct::symbol }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.FieldNames(mockType, distinct = true)
        val actual = reader.readConstraint(context, struct["field_names"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading a valid field_names constraint with '$null_or' should return a FieldNames instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = FieldNamesReader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ field_names: ${'$'}null_or::symbol }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.FieldNames(mockType)
        val actual = reader.readConstraint(context, struct["field_names"])
        kotlin.test.assertEquals(expected, actual)
    }

    @Test
    fun `reading a field_names constraint with any annotation other than 'distinct' or '$null_or' should throw exception`() {
        val typeReader = mockk<TypeReader>()
        val reader = FieldNamesReader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ field_names: foo::symbol }""") as IonStruct
        val context = ReaderContext()

        assertThrows<InvalidSchemaException> {
            reader.readConstraint(context, struct["field_names"])
        }
    }
}
