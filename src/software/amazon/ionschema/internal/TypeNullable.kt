package software.amazon.ionschema.internal

import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations

/**
 * [Type] decorator that implements the nullable:: annotation.
 */
internal class TypeNullable(
        override val ion: IonValue,
        private val type: TypeInternal,
        schema: Schema
) : TypeInternal by type, ConstraintBase(ion) {

    init {
        if (type.getBaseType() == schema.getType("document")) {
            throw InvalidSchemaException("The 'document' type is not nullable")
        }
    }

    override fun validate(value: IonValue, issues: Violations) {
        if (!(value.isNullValue
                    && (value.type == IonType.NULL || type.isValidForBaseType(value)))) {
            (type as ConstraintInternal).validate(value, issues)
        }
    }

    override fun name() = type.name()
}
