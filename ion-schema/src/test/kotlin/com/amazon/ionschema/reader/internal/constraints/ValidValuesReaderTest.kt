package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.Timestamp
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.ConsistentDecimal
import com.amazon.ionschema.model.ConsistentTimestamp
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ContinuousRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.ValidValue
import com.amazon.ionschema.reader.internal.ReaderContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

@OptIn(ExperimentalIonSchemaModel::class)
class ValidValuesReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'valid_values'`() {
        assertTrue(ValidValuesReader().canRead("valid_values"))
    }

    @Test
    fun `canRead should return false for any field other than 'valid_values'`() {
        val reader = ValidValuesReader()
        Assertions.assertFalse(reader.canRead("valeurs_valides"))
    }

    @Test
    fun `reading a field that is not a supported field should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = ValidValuesReader()
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reader should be able to read a list of values`() {
        val struct = ION.singleValue("""{ valid_values: [3, range::[1, 10], range::[2022T, 2024T]] }""") as IonStruct
        val reader = ValidValuesReader()
        val context = ReaderContext()
        val expected = Constraint.ValidValues(
            setOf(
                ValidValue.Value(ION.newInt(3)),
                ValidValue.NumberRange(
                    ContinuousRange.Limit.Closed(ConsistentDecimal(BigDecimal.ONE)),
                    ContinuousRange.Limit.Closed(ConsistentDecimal(BigDecimal.TEN)),
                ),
                ValidValue.TimestampRange(
                    ContinuousRange.Limit.Closed(ConsistentTimestamp(Timestamp.forDay(2022, 1, 1))),
                    ContinuousRange.Limit.Closed(ConsistentTimestamp(Timestamp.forDay(2024, 1, 1))),
                ),
            )
        )

        val result = reader.readConstraint(context, struct["valid_values"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range`() {
        val struct = ION.singleValue("""{ valid_values: range::[1, 10] }""") as IonStruct
        val reader = ValidValuesReader()
        val context = ReaderContext()
        val expected = Constraint.ValidValues(
            setOf(
                ValidValue.NumberRange(
                    ContinuousRange.Limit.Closed(ConsistentDecimal(BigDecimal.ONE)),
                    ContinuousRange.Limit.Closed(ConsistentDecimal(BigDecimal.TEN)),
                )
            )
        )
        val result = reader.readConstraint(context, struct["valid_values"])
        assertEquals(expected, result)
    }
}
