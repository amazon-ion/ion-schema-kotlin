package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.Ieee754InterchangeFormat
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.invalidConstraint

@ExperimentalIonSchemaModel
internal class Ieee754FloatReader : ConstraintReader {
    override fun canRead(fieldName: String): Boolean = fieldName == "ieee754_float"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        islRequireNoIllegalAnnotations(field) { invalidConstraint(field, "must not have annotations") }
        val format = when ((field as? IonSymbol)?.stringValue()) {
            "binary16" -> Ieee754InterchangeFormat.Binary16
            "binary32" -> Ieee754InterchangeFormat.Binary32
            "binary64" -> Ieee754InterchangeFormat.Binary64
            else -> throw InvalidSchemaException(invalidConstraint(field, "must be one of 'binary16', 'binary32', 'binary64'"))
        }

        return Constraint.Ieee754Float(format)
    }
}
