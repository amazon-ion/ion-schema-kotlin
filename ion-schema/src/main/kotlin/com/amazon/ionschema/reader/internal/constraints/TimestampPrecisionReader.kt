package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonValue
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.toTimestampPrecisionRange

@ExperimentalIonSchemaModel
internal class TimestampPrecisionReader : ConstraintReader {

    override fun canRead(fieldName: String): Boolean = fieldName == "timestamp_precision"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))
        return Constraint.TimestampPrecision(field.toTimestampPrecisionRange())
    }
}
