package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonList
import com.amazon.ion.IonValue
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.VariablyOccurringTypeArgument.Companion.OCCURS_REQUIRED
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.TypeReader
import com.amazon.ionschema.reader.internal.invalidConstraint

@ExperimentalIonSchemaModel
internal class OrderedElementsReader(private val typeReader: TypeReader) : ConstraintReader {

    override fun canRead(fieldName: String): Boolean = fieldName == "ordered_elements"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        islRequireIonTypeNotNull<IonList>(field) { invalidConstraint(field, "must be a non-null list") }
        islRequireNoIllegalAnnotations(field) { invalidConstraint(field, "must not have annotations") }
        return Constraint.OrderedElements(
            field.map { typeReader.readVariablyOccurringTypeArg(context, it, defaultOccurs = OCCURS_REQUIRED) }
        )
    }
}
