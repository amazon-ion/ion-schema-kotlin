package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonList
import com.amazon.ion.IonNumber
import com.amazon.ion.IonTimestamp
import com.amazon.ion.IonValue
import com.amazon.ionschema.InvalidSchemaException
import com.amazon.ionschema.internal.util.islRequireIonTypeNotNull
import com.amazon.ionschema.internal.util.islRequireNoIllegalAnnotations
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.model.ValidValue
import com.amazon.ionschema.reader.internal.ReaderContext
import com.amazon.ionschema.reader.internal.invalidConstraint
import com.amazon.ionschema.reader.internal.toNumberRange
import com.amazon.ionschema.reader.internal.toTimestampRange

@ExperimentalIonSchemaModel
internal class ValidValuesReader : ConstraintReader {

    override fun canRead(fieldName: String): Boolean = fieldName == "valid_values"

    override fun readConstraint(context: ReaderContext, field: IonValue): Constraint {
        check(canRead(field.fieldName))

        islRequireIonTypeNotNull<IonList>(field) { invalidConstraint(field, "must be a non-null list") }
        islRequireNoIllegalAnnotations(field, "range") { invalidConstraint(field, "must be a range or an unannotated list of values ") }
        val theList = if (field.hasTypeAnnotation("range")) listOf(field) else field

        val theValidValues = theList.map {
            if (it.hasTypeAnnotation("range") && it is IonList) {
                when {
                    it.any { x -> x is IonTimestamp } -> ValidValue.IonTimestampRange(it.toTimestampRange())
                    it.any { x -> x is IonNumber } -> ValidValue.IonNumberRange(it.toNumberRange())
                    else -> throw InvalidSchemaException("Not a valid range: $it")
                }
            } else {
                islRequireNoIllegalAnnotations(it) { invalidConstraint(it, "annotations not permitted except for range") }
                ValidValue.Value(it.clone())
            }
        }

        return Constraint.ValidValues(theValidValues)
    }
}
