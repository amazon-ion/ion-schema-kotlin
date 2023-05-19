package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonValue
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader

@ExperimentalIonSchemaModel
internal class LogicConstraintsReader(private val typeReader: TypeReader) : ConstraintReader {
    companion object {
        private val CONSTRAINT_NAMES = setOf("all_of", "any_of", "not", "one_of", "type")
    }

    override fun canRead(fieldName: String): Boolean = fieldName in CONSTRAINT_NAMES

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        return when (field.fieldName) {
            "all_of" -> Constraint.AllOf(typeReader.readTypeArgumentList(context, field))
            "any_of" -> Constraint.AnyOf(typeReader.readTypeArgumentList(context, field))
            "not" -> Constraint.Not(typeReader.readTypeArg(context, field,))
            "one_of" -> Constraint.OneOf(typeReader.readTypeArgumentList(context, field))
            "type" -> Constraint.Type(typeReader.readTypeArg(context, field))
            else -> TODO("Unreachable")
        }
    }
}
