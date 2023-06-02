package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TypeArgument
import com.amazon.ionschema.model.TypeDefinition
import com.amazon.ionschema.model.ValidValue
import com.amazon.ionschema.model.mapToSet
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalIonSchemaModel
class AnnotationsV2ReaderTest {

    private companion object {
        val ION = IonSystemBuilder.standard().build()
    }

    @Test
    fun `canRead should return true for 'annotations'`() {
        val reader = AnnotationsV2Reader(mockk())
        assertTrue(reader.canRead("annotations"))
    }

    @Test
    fun `canRead should return false for any field other than 'annotations'`() {
        val reader = AnnotationsV2Reader(mockk())
        assertFalse(reader.canRead("bananotations"))
    }

    @Test
    fun `reading a field that is not named 'annotations' should throw IllegalStateException`() {
        val reader = AnnotationsV2Reader(mockk())

        val struct = ION.singleValue("""{ foo: symbol }""") as IonStruct
        val context = ReaderContext()
        assertThrows<IllegalStateException> {
            reader.readConstraint(context, struct["foo"])
        }
    }

    @Test
    fun `reading an annotations constraint with a type argument should return an AnnotationsV2 instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = AnnotationsV2Reader(typeReader)
        val mockType = mockk<TypeArgument>()

        every { typeReader.readTypeArg(any(), any(), any()) } returns mockType

        val struct = ION.singleValue("""{ annotations: { valid_values: [[a, b, c]] } }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.AnnotationsV2(mockType)
        val actual = reader.readConstraint(context, struct["annotations"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading an annotations constraint with a list of required annotations should return an AnnotationsV2 instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = AnnotationsV2Reader(typeReader)

        val struct = ION.singleValue("""{ annotations: required::[a, b, c] }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.AnnotationsV2(
            TypeArgument.InlineType(
                TypeDefinition(
                    setOf(
                        Constraint.Contains(setOf("a", "b", "c").mapToSet(ION::newSymbol))
                    )
                )
            )
        )
        val actual = reader.readConstraint(context, struct["annotations"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading an annotations constraint with a list of closed annotations should return an AnnotationsV2 instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = AnnotationsV2Reader(typeReader)

        val struct = ION.singleValue("""{ annotations: closed::[a, b, c] }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.AnnotationsV2(
            TypeArgument.InlineType(
                TypeDefinition(
                    setOf(
                        Constraint.Element(
                            TypeArgument.InlineType(
                                TypeDefinition(
                                    setOf(
                                        Constraint.ValidValues(setOf("a", "b", "c").mapToSet { ValidValue.Value(ION.newSymbol(it)) })
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        val actual = reader.readConstraint(context, struct["annotations"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading an annotations constraint with a list of closed and required annotations should return an AnnotationsV2 instance`() {
        val typeReader = mockk<TypeReader>()
        val reader = AnnotationsV2Reader(typeReader)

        val struct = ION.singleValue("""{ annotations: closed::required::[a, b, c] }""") as IonStruct
        val context = ReaderContext()

        val expected = Constraint.AnnotationsV2(
            TypeArgument.InlineType(
                TypeDefinition(
                    setOf(
                        Constraint.Contains(setOf("a", "b", "c").mapToSet(ION::newSymbol)),
                        Constraint.Element(
                            TypeArgument.InlineType(
                                TypeDefinition(
                                    setOf(
                                        Constraint.ValidValues(setOf("a", "b", "c").mapToSet { ValidValue.Value(ION.newSymbol(it)) })
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        val actual = reader.readConstraint(context, struct["annotations"])
        assertEquals(expected, actual)
    }

    @Test
    fun `reading an annotations constraint with invalid annotation on list should throw exception`() {
        val typeReader = mockk<TypeReader>()
        val reader = AnnotationsV2Reader(typeReader)

        val struct = ION.singleValue("""{ annotations: flux_capacitor::[a, b, c] }""") as IonStruct
        val context = ReaderContext()

        assertThrows<InvalidSchemaException> {
            reader.readConstraint(context, struct["annotations"])
        }
    }

    @Test
    fun `reading an annotations constraint with no annotation on list should throw exception`() {
        val typeReader = mockk<TypeReader>()
        val reader = AnnotationsV2Reader(typeReader)

        val struct = ION.singleValue("""{ annotations: [a, b, c] }""") as IonStruct
        val context = ReaderContext()

        assertThrows<InvalidSchemaException> {
            reader.readConstraint(context, struct["annotations"])
        }
    }

    @Test
    fun `reading an annotations constraint with invalid list contents should throw exception`() {
        val typeReader = mockk<TypeReader>()
        val reader = AnnotationsV2Reader(typeReader)

        val struct = ION.singleValue("""{ annotations: closed::[a, 1, 2023-04-05T06:07:08Z] }""") as IonStruct
        val context = ReaderContext()

        assertThrows<InvalidSchemaException> {
            reader.readConstraint(context, struct["annotations"])
        }
    }
}
