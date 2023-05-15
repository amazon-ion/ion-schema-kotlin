package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalIonSchemaModel
class RegexReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
        val READER_1 = RegexReader(IonSchemaVersion.v1_0)
        val READER_2 = RegexReader(IonSchemaVersion.v2_0)
    }

    @Test
    fun `canRead should return true for 'regex'`() {
        Assertions.assertTrue(READER_1.canRead("regex"))
        Assertions.assertTrue(READER_2.canRead("regex"))
    }

    @Test
    fun `canRead should return false for any field other than 'regex'`() {
        Assertions.assertFalse(READER_1.canRead("xeger"))
        Assertions.assertFalse(READER_2.canRead("xeger"))
    }

    @Test
    fun `reading a field that is not named 'regex' should throw IllegalStateException`() {
        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val context = ReaderContext()
        assertThrows<IllegalStateException> { READER_1.readConstraint(context, struct["foo"]) }
        assertThrows<IllegalStateException> { READER_2.readConstraint(context, struct["foo"]) }
    }

    @Test
    fun `read regex with invalid pattern should throw exception`() {
        val struct = ION.singleValue("""{ regex: "**{([" }""") as IonStruct
        val context = ReaderContext()
        assertThrows<IllegalArgumentException> { READER_1.readConstraint(context, struct["regex"]) }
        assertThrows<IllegalArgumentException> { READER_2.readConstraint(context, struct["regex"]) }
    }

    @Test
    fun `read regex with invalid modifiers should throw exception`() {
        // only valid modifier annotations are 'i' and 'm'
        val struct = ION.singleValue("""{ regex: Q::"abc" }""") as IonStruct
        val context = ReaderContext()
        assertThrows<InvalidSchemaException> { READER_1.readConstraint(context, struct["regex"]) }
        assertThrows<InvalidSchemaException> { READER_2.readConstraint(context, struct["regex"]) }
    }

    @Test
    fun `read regex with unsupported feature for ISL 1,0 should throw exception`() {
        // character class meta-chars inside a character set are not supported until Ion Schema 2.0
        val struct = ION.singleValue("""{ regex: "[a-f\\d]" }""") as IonStruct
        val context = ReaderContext() // val result = READER_1.readConstraint(context, struct["regex"])
        assertThrows<InvalidSchemaException> { READER_1.readConstraint(context, struct["regex"]) }
    }

    @Test
    fun `read valid regex should return regex constraint for ISL 1,0`() {
        val struct = ION.singleValue("""{ regex: "^[0-9a-f]+$" }""") as IonStruct
        val context = ReaderContext()
        val expected = Constraint.Regex("^[0-9a-f]+$", ionSchemaVersion = IonSchemaVersion.v1_0)
        val actual = READER_1.readConstraint(context, struct["regex"])
        assertEquals(expected, actual)
    }

    @Test
    fun `read valid regex should return regex constraint for ISL 2,0`() {
        val struct = ION.singleValue("""{ regex: "^[0-9a-f]+$" }""") as IonStruct
        val context = ReaderContext()
        val expected = Constraint.Regex("^[0-9a-f]+$", ionSchemaVersion = IonSchemaVersion.v2_0)
        val actual = READER_2.readConstraint(context, struct["regex"])
        assertEquals(expected, actual)
    }

    @Test
    fun `read regex should correctly handle multiline modifier`() {
        val struct = ION.singleValue("""{ regex: m::"^[0-9a-f]+$" }""") as IonStruct
        val context = ReaderContext()
        val expected = Constraint.Regex("^[0-9a-f]+$", multiline = true, ionSchemaVersion = IonSchemaVersion.v2_0)
        val actual = READER_2.readConstraint(context, struct["regex"])
        assertEquals(expected, actual)
    }

    @Test
    fun `read regex should correctly handle case-insensitive modifier`() {
        val struct = ION.singleValue("""{ regex: i::"^[0-9a-f]+$" }""") as IonStruct
        val context = ReaderContext()
        val expected = Constraint.Regex("^[0-9a-f]+$", caseInsensitive = true, ionSchemaVersion = IonSchemaVersion.v2_0)
        val actual = READER_2.readConstraint(context, struct["regex"])
        assertEquals(expected, actual)
    }
}
