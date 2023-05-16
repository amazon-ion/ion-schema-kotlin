package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.DiscreteIntRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@OptIn(ExperimentalIonSchemaModel::class)
class LengthConstraintsReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @ValueSource(strings = ["byte_length", "codepoint_length", "container_length", "utf8_byte_length"])
    @ParameterizedTest(name = "canRead should return true for {0}")
    fun `canRead should return true for supported constraints`(fieldName: String) {
        assertTrue(LengthConstraintsReader().canRead(fieldName))
    }

    @Test
    fun `canRead should return false for unsupported constraints`() {
        val reader = LengthConstraintsReader()
        Assertions.assertFalse(reader.canRead("planck_length"))
    }

    @Test
    fun `reading a field that is not a supported field should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = LengthConstraintsReader()
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reader should be able to read a single value`() {
        val struct = ION.singleValue("""{ byte_length: 3 }""") as IonStruct
        val reader = LengthConstraintsReader()
        val context = ReaderContext()
        val expected = Constraint.ByteLength(DiscreteIntRange(3))
        val result = reader.readConstraint(context, struct["byte_length"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range`() {
        val struct = ION.singleValue("""{ byte_length: range::[1, 4] }""") as IonStruct
        val reader = LengthConstraintsReader()
        val context = ReaderContext()
        val expected = Constraint.ByteLength(DiscreteIntRange(1, 4))
        val result = reader.readConstraint(context, struct["byte_length"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with unbounded upper endpoint`() {
        val struct = ION.singleValue("""{ codepoint_length: range::[1, max] }""") as IonStruct
        val reader = LengthConstraintsReader()
        val context = ReaderContext()
        val expected = Constraint.CodepointLength(DiscreteIntRange(1, null))
        val result = reader.readConstraint(context, struct["codepoint_length"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with unbounded lower endpoint`() {
        val struct = ION.singleValue("""{ container_length: range::[min, 4] }""") as IonStruct
        val reader = LengthConstraintsReader()
        val context = ReaderContext()
        val expected = Constraint.ContainerLength(DiscreteIntRange(null, 4))
        val result = reader.readConstraint(context, struct["container_length"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with exclusive endpoint`() {
        val struct = ION.singleValue("""{ utf8_byte_length: range::[1, exclusive::5] }""") as IonStruct
        val reader = LengthConstraintsReader()
        val context = ReaderContext()
        val expected = Constraint.Utf8ByteLength(DiscreteIntRange(1, 4))
        val result = reader.readConstraint(context, struct["utf8_byte_length"])
        assertEquals(expected, result)
    }
}
