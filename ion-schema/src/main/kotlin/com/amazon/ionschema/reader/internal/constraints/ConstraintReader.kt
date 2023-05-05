package com.amazon.ionschema.reader.internal.constraints

import com.amazon.ion.IonValue
import com.amazon.ionschema.model.Constraint
import com.amazon.ionschema.model.ExperimentalIonSchemaModel
import com.amazon.ionschema.reader.internal.ReaderContext

/**
 * Allows us to compose TypeReaders out of different combinations of constraint readers to enable code reuse across
 * multiple Ion Schema versions.
 */
@ExperimentalIonSchemaModel
internal interface ConstraintReader {

    /**
     * Contract—return null if and only if:
     * - this ConstraintReader implementation does not handle the given field name _OR_
     * - this ConstraintReader implementation does handle the given field name, but the value was not valid in some
     *   way AND at least one error has been added to the context.
     */
    fun readConstraint(context: ReaderContext, constraintField: IonValue): Constraint?
}
