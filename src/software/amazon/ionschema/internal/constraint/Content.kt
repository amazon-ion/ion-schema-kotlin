package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Violations

/**
 * Implements the content constraint.
 *
 * This implementation exists solely to verify that the constraint
 * definition is valid.  Validation logic for this constraint is
 * performed by the [Fields] constraint.
 *
 * @see https://amzn.github.io/ion-schema/docs/spec.html#content
 */
internal class Content(
        ion: IonValue
) : ConstraintBase(ion) {

    init {
        if (!(ion is IonSymbol
                    && !ion.isNullValue
                    && ion.stringValue() == "closed")) {
            throw InvalidSchemaException("Invalid content constraint: $ion")
        }
    }

    override fun validate(value: IonValue, issues: Violations) {
        // no-op, validation logic for this constraint is performed by the fields constraint
    }
}

