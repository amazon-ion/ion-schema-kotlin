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

@OptIn(ExperimentalIonSchemaModel::class)
class PrecisionReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'precision'`() {
        assertTrue(PrecisionReader().canRead("precision"))
    }

    @Test
    fun `canRead should return false for any field other than 'precision'`() {
        val reader = PrecisionReader()
        Assertions.assertFalse(reader.canRead("scale"))
    }

    @Test
    fun `reading a field that is not a supported field should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = PrecisionReader()
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reader should be able to read a single value`() {
        val struct = ION.singleValue("""{ precision: 3 }""") as IonStruct
        val reader = PrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.Precision(DiscreteIntRange(3))
        val result = reader.readConstraint(context, struct["precision"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range`() {
        val struct = ION.singleValue("""{ precision: range::[1, 4] }""") as IonStruct
        val reader = PrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.Precision(DiscreteIntRange(1, 4))
        val result = reader.readConstraint(context, struct["precision"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with unbounded upper endpoint`() {
        val struct = ION.singleValue("""{ precision: range::[1, max] }""") as IonStruct
        val reader = PrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.Precision(DiscreteIntRange(1, null))
        val result = reader.readConstraint(context, struct["precision"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with unbounded lower endpoint`() {
        val struct = ION.singleValue("""{ precision: range::[min, 4] }""") as IonStruct
        val reader = PrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.Precision(DiscreteIntRange(null, 4))
        val result = reader.readConstraint(context, struct["precision"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with exclusive endpoint`() {
        val struct = ION.singleValue("""{ precision: range::[1, exclusive::5] }""") as IonStruct
        val reader = PrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.Precision(DiscreteIntRange(1, 4))
        val result = reader.readConstraint(context, struct["precision"])
        assertEquals(expected, result)
    }
}
