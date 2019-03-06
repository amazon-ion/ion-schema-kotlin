package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Violations

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

