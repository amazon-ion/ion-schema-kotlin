package software.amazon.ionschema.internal

import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations

internal class TypeNullable(
        ion: IonValue,
        private val type: TypeInternal
) : TypeInternal by type, ConstraintBase(ion) {

    override fun validate(value: IonValue, issues: Violations) {
        if (!(value.isNullValue
                    && (value.type == IonType.NULL || type.isValidForBaseType(value)))) {
            type.validate(value, issues)
        }
    }

    override fun name() = type.name()
}
