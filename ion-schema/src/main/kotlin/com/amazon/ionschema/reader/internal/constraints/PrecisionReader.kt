package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonValue
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.toDiscreteIntRange

@ExperimentalIonSchemaModel
internal class PrecisionReader : ConstraintReader {
    override fun canRead(fieldName: String): Boolean = fieldName == "precision"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))
        return Constraint.Precision(field.toDiscreteIntRange())
    }
}
