package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonList
import com.amazon.ion.IonString
import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireElementType
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.internal.util.islRequireNotEmpty
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.TimestampOffsetValue
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.invalidConstraint

@ExperimentalIonSchemaModel
internal class TimestampOffsetReader : ConstraintReader {

    override fun canRead(fieldName: String): Boolean = fieldName == "timestamp_offset"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        islRequireIonTypeNotNull<IonList>(field) { invalidConstraint(field, "must be a non-null, non-empty list") }
        islRequire(field.isNotEmpty()) { invalidConstraint(field, "must be a non-null, non-empty list") }
        islRequireNoIllegalAnnotations(field) { invalidConstraint(field, "must not have annotations") }
        return Constraint.TimestampOffset(
            field.islRequireElementType<IonString>("timestamp offset list")
                .islRequireNotEmpty("timestamp offset list")
                .map { TimestampOffsetValue.parse(it.stringValue()) }
        )
    }
}
