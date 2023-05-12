package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.Ieee754InterchangeFormat
import com.amazon.ionschema.reader.internal.ReaderContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalIonSchemaModel
class Ieee745FloatReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'ieee754_float'`() {
        val reader = Ieee754FloatReader()
        Assertions.assertTrue(reader.canRead("ieee754_float"))
    }

    @Test
    fun `canRead should return false for any field other than 'ieee754_float'`() {
        val reader = Ieee754FloatReader()
        Assertions.assertFalse(reader.canRead("ieee754_float_boat"))
    }

    @Test
    fun `reading a field that is not named 'ieee754_float' should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val reader = Ieee754FloatReader()
        val context = ReaderContext()
        assertThrows<IllegalStateException> { reader.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `read ieee754_float with unknown float format should throw exception`() {
        val struct = ION.singleValue("""{ ieee754_float: binary1995 }""") as IonStruct
        val reader = Ieee754FloatReader()
        val context = ReaderContext()
        assertThrows<InvalidSchemaException> { reader.readConstraint(context, struct["ieee754_float"]) }
    }

    @Test
    fun `read ieee754_float with annotation should throw exception`() {
        val struct = ION.singleValue("""{ ieee754_float: foo::binary32 }""") as IonStruct
        val reader = Ieee754FloatReader()
        val context = ReaderContext()
        assertThrows<InvalidSchemaException> { reader.readConstraint(context, struct["ieee754_float"]) }
    }

    @Test
    fun `read ieee754_float with binary16 should return ieee754_float constraint`() {
        val struct = ION.singleValue("""{ ieee754_float: binary16 }""") as IonStruct
        val reader = Ieee754FloatReader()
        val context = ReaderContext()
        val actual = reader.readConstraint(context, struct["ieee754_float"])
        assertEquals(Constraint.Ieee754Float(Ieee754InterchangeFormat.Binary16), actual)
    }

    @Test
    fun `read ieee754_float with binary32 should return ieee754_float constraint`() {
        val struct = ION.singleValue("""{ ieee754_float: binary32 }""") as IonStruct
        val reader = Ieee754FloatReader()
        val context = ReaderContext()
        val actual = reader.readConstraint(context, struct["ieee754_float"])
        assertEquals(Constraint.Ieee754Float(Ieee754InterchangeFormat.Binary32), actual)
    }

    @Test
    fun `read ieee754_float with binary64 should return ieee754_float constraint`() {
        val struct = ION.singleValue("""{ ieee754_float: binary64 }""") as IonStruct
        val reader = Ieee754FloatReader()
        val context = ReaderContext()
        val actual = reader.readConstraint(context, struct["ieee754_float"])
        assertEquals(Constraint.Ieee754Float(Ieee754InterchangeFormat.Binary64), actual)
    }
}
