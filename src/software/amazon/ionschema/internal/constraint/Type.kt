package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonValue
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.TypeReference

internal class Type(
        ion: IonValue,
        schema: Schema
) : ConstraintBase(ion) {

    private val typeReference = TypeReference.create(ion, schema)

    override fun validate(value: IonValue, issues: Violations) = typeReference().validate(value, issues)
}

