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
     * Returns true if this constraint reader can read the given constraint.
     */
    fun canRead(fieldName: String): Boolean

    /**
     * Returns a [Constraint] instance for the given field.
     * Should only be called after checking whether the constraint is supported by calling [canRead].
     * Must throw [IllegalStateException] if called for an unsupported field name.
     */
    fun readConstraint(context: ReaderContext, field: IonValue): Constraint
}
