package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonValue
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.toDiscreteIntRange

@ExperimentalIonSchemaModel
internal class LengthConstraintsReader : ConstraintReader {
    companion object {
        private val CONSTRAINT_NAMES = setOf("byte_length", "codepoint_length", "container_length", "utf8_byte_length")
    }

    override fun canRead(fieldName: String): Boolean = fieldName in CONSTRAINT_NAMES

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        val range = field.toDiscreteIntRange()

        return when (field.fieldName) {
            "byte_length" -> Constraint.ByteLength(range)
            "codepoint_length" -> Constraint.CodepointLength(range)
            "container_length" -> Constraint.ContainerLength(range)
            "utf8_byte_length" -> Constraint.Utf8ByteLength(range)
            else -> TODO("Unreachable!")
        }
    }
}
