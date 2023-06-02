package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TimestampOffsetValue
import com.amazon.ionschema.reader.internal.ReaderContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalIonSchemaModel::class)
class TimestampOffsetReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'timestamp_offset'`() {
        assertTrue(TimestampOffsetReader().canRead("timestamp_offset"))
    }

    @Test
    fun `canRead should return false for any field other than 'timestamp_offset'`() {
        val reader = TimestampOffsetReader()
        Assertions.assertFalse(reader.canRead("stardate_offset"))
    }

    @Test
    fun `reading a field that is not a supported field should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = TimestampOffsetReader()
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `reader should be able to read some timestamp offsets`() {
        val struct = ION.singleValue("""{ timestamp_offset: ["+00:30", "+01:30"] }""") as IonStruct
        val reader = TimestampOffsetReader()
        val context = ReaderContext()
        val expected = Constraint.TimestampOffset(
            setOf(
                TimestampOffsetValue.fromMinutes(30),
                TimestampOffsetValue.fromMinutes(90),
            )
        )
        val result = reader.readConstraint(context, struct["timestamp_offset"])
        assertEquals(expected, result)
    }
}
