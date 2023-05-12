package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonString
import com.amazon.ion.IonValue
import com.amazon.ionschema.IonSchemaVersion
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.invalidConstraint

@ExperimentalIonSchemaModel
internal class RegexReader(private val ionSchemaVersion: IonSchemaVersion) : ConstraintReader {
    override fun canRead(fieldName: String): Boolean = fieldName == "regex"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        islRequireIonTypeNotNull<IonString>(field) { invalidConstraint(field, "must be a non-null string") }
        islRequireNoIllegalAnnotations(field, "i", "m") { invalidConstraint(field, "regex pattern may only be annotated with 'i' and 'm'") }

        return Constraint.Regex(
            field.stringValue(),
            multiline = field.hasTypeAnnotation("m"),
            caseInsensitive = field.hasTypeAnnotation("i"),
            ionSchemaVersion = ionSchemaVersion
        )
    }
}
