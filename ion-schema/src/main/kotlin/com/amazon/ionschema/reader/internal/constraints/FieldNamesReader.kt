package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader
import com.amazon.ionschema.reader.internal.invalidConstraint

/**
 * Reads the `element` constraint for ISL 2.0 and higher
 */
@ExperimentalIonSchemaModel
internal class FieldNamesReader(private val typeReader: TypeReader) : ConstraintReader {
    override fun canRead(fieldName: String): Boolean = fieldName == "field_names"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        islRequireNoIllegalAnnotations(field, "distinct", "\$null_or") {
            invalidConstraint(
                field,
                "type argument may only be annotated with 'distinct' or '\$null_or'"
            )
        }
        val typeArg = typeReader.readTypeArg(context, field, checkAnnotations = false)

        return Constraint.FieldNames(typeArg, distinct = field.hasTypeAnnotation("distinct"))
    }
}
