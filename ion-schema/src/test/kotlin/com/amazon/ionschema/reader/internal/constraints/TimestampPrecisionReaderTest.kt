package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ContinuousRange
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TimestampPrecisionRange
import com.amazon.ionschema.model.TimestampPrecisionValue
import com.amazon.ionschema.reader.internal.ReaderContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalIonSchemaModel::class)
class TimestampPrecisionReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'timestamp_precision'`() {
        assertTrue(TimestampPrecisionReader().canRead("timestamp_precision"))
    }

    @Test
    fun `canRead should return false for any field other than 'timestamp_precision'`() {
        val reader = TimestampPrecisionReader()
        Assertions.assertFalse(reader.canRead("stardate_precision"))
    }

    @Test
    fun `reading a field that is not a supported field should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = TimestampPrecisionReader()
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reader should be able to read a single value`() {
        val struct = ION.singleValue("""{ timestamp_precision: year }""") as IonStruct
        val reader = TimestampPrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.TimestampPrecision(TimestampPrecisionRange(TimestampPrecisionValue.Year))
        val result = reader.readConstraint(context, struct["timestamp_precision"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range`() {
        val struct = ION.singleValue("""{ timestamp_precision: range::[month, day] }""") as IonStruct
        val reader = TimestampPrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.TimestampPrecision(
            TimestampPrecisionRange(
                ContinuousRange.Limit.Closed(TimestampPrecisionValue.Month),
                ContinuousRange.Limit.Closed(TimestampPrecisionValue.Day),
            )
        )
        val result = reader.readConstraint(context, struct["timestamp_precision"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with unbounded upper endpoint`() {
        val struct = ION.singleValue("""{ timestamp_precision: range::[minute, max] }""") as IonStruct
        val reader = TimestampPrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.TimestampPrecision(
            TimestampPrecisionRange(
                ContinuousRange.Limit.Closed(TimestampPrecisionValue.Minute),
                ContinuousRange.Limit.Unbounded,
            )
        )
        val result = reader.readConstraint(context, struct["timestamp_precision"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with unbounded lower endpoint`() {
        val struct = ION.singleValue("""{ timestamp_precision: range::[min, second] }""") as IonStruct
        val reader = TimestampPrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.TimestampPrecision(
            TimestampPrecisionRange(
                ContinuousRange.Limit.Unbounded,
                ContinuousRange.Limit.Closed(TimestampPrecisionValue.Second),
            )
        )
        val result = reader.readConstraint(context, struct["timestamp_precision"])
        assertEquals(expected, result)
    }

    @Test
    fun `reader should be able to read a range with exclusive endpoint`() {
        val struct = ION.singleValue("""{ timestamp_precision: range::[millisecond, exclusive::microsecond] }""") as IonStruct
        val reader = TimestampPrecisionReader()
        val context = ReaderContext()
        val expected = Constraint.TimestampPrecision(
            TimestampPrecisionRange(
                ContinuousRange.Limit.Closed(TimestampPrecisionValue.Millisecond),
                ContinuousRange.Limit.Open(TimestampPrecisionValue.Microsecond),
            )
        )
        val result = reader.readConstraint(context, struct["timestamp_precision"])
        assertEquals(expected, result)
    }
}
