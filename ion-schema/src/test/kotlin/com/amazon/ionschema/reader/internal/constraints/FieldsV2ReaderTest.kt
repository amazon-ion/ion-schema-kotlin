package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.VariablyOccurringTypeArgument
import com.amazon.ionschema.model.VariablyOccurringTypeArgument.Companion.OCCURS_OPTIONAL
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
class FieldsV2ReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'fields'`() {
        assertTrue(FieldsV2Reader(mockk()).canRead("fields"))
    }

    @Test
    fun `canRead should return false for any field other than 'fields'`() {
        val reader = FieldsV2Reader(mockk())
        Assertions.assertFalse(reader.canRead("meadows"))
    }

    @Test
    fun `reading a field that is not a supported field should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = FieldsV2Reader(mockk())
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reading a valid fields constraint should return a Fields instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = FieldsV2Reader(typeReader)
        val mockTypeString = mockk<VariablyOccurringTypeArgument>()
        val context = ReaderContext()

        val struct = ION.singleValue("""{ fields: { foo: string } }""") as IonStruct

        every { typeReader.readVariablyOccurringTypeArg(context, ION.newSymbol("string"), OCCURS_OPTIONAL) } returns mockTypeString

        val expected = Constraint.Fields(mapOf("foo" to mockTypeString), closed = false)

        val actual = reader.readConstraint(context, struct["fields"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading a valid fields constraint with 'closed' should return a Fields instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = FieldsV2Reader(typeReader)
        val mockTypeString = mockk<VariablyOccurringTypeArgument>()
        val mockTypeInt = mockk<VariablyOccurringTypeArgument>()
        val context = ReaderContext()

        val struct = ION.singleValue("""{ fields: closed::{ foo: string, bar: int } }""") as IonStruct

        every { typeReader.readVariablyOccurringTypeArg(context, ION.newSymbol("string"), OCCURS_OPTIONAL) } returns mockTypeString
        every { typeReader.readVariablyOccurringTypeArg(context, ION.newSymbol("int"), OCCURS_OPTIONAL) } returns mockTypeInt

        val expected = Constraint.Fields(
            mapOf(
                "foo" to mockTypeString,
                "bar" to mockTypeInt,
            ),
            closed = true
        )

        val actual = reader.readConstraint(context, struct["fields"])
        assertEquals(expected, actual)
    }
}
