package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.internal.util.islRequire
import com.amazon.ionschema.internal.util.islRequireElementType
import com.amazon.ionschema.internal.util.islRequireIonNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader
import com.amazon.ionschema.reader.internal.invalidConstraint

@ExperimentalIonSchemaModel
internal class AnnotationsV2Reader(private val typeReader: TypeReader) : ConstraintReader {
    override fun canRead(fieldName: String): Boolean = fieldName == "annotations"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        return when (field) {
            is IonList -> {
                islRequireIonNotNull(field) { invalidConstraint(field, "must not be null.list") }
                islRequireNoIllegalAnnotations(field, "closed", "required") {
                    invalidConstraint(field, "list of annotations may not be annotated other than 'closed' and 'required'")
                }
                islRequire(field.typeAnnotations.isNotEmpty()) {
                    invalidConstraint(field, "list of annotations must be annotated with at least one of 'closed' and 'required'")
                }
                field.islRequireElementType<IonSymbol>("list of annotations")

                val modifier = if (field.hasTypeAnnotation("closed") && field.hasTypeAnnotation("required")) {
                    Constraint.AnnotationsV2.Modifier.ClosedAndRequired
                } else if (field.hasTypeAnnotation("required")) {
                    Constraint.AnnotationsV2.Modifier.Required
                } else {
                    Constraint.AnnotationsV2.Modifier.Closed
                }
                Constraint.AnnotationsV2.create(modifier, field.filterIsInstance<IonSymbol>())
            }
            is IonStruct, is IonSymbol -> Constraint.AnnotationsV2(typeReader.readTypeArg(context, field))
            else -> throw InvalidSchemaException(invalidConstraint(field, "must be a type argument (symbol or struct) or a list of valid annotations"))
        }
    }
}
