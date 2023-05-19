package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonStruct
import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.VariablyOccurringTypeArgument
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader
import com.amazon.ionschema.reader.internal.invalidConstraint

/**
 * Reads the `fields` constraint for ISL 2.0 and higher
 */
@ExperimentalIonSchemaModel
internal class FieldsV2Reader(private val typeReader: TypeReader) : ConstraintReader {
    override fun canRead(fieldName: String): Boolean = fieldName == "fields"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        islRequireIonTypeNotNull<IonStruct>(field) { invalidConstraint(field, "must be a non-null struct") }
        islRequireNoIllegalAnnotations(field, "closed") {
            invalidConstraint(field, "argument may only be annotated with 'closed'")
        }
        require(!field.isEmpty)
        require(field.map { it.fieldName }.distinct().size == field.size())
        return Constraint.Fields(
            fields = field.associate {
                it.fieldName to typeReader.readVariablyOccurringTypeArg(
                    context,
                    it,
                    VariablyOccurringTypeArgument.OCCURS_OPTIONAL
                )
            },
            closed = field.hasTypeAnnotation("closed")
        )
    }
}
