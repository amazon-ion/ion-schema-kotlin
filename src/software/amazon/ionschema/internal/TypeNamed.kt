package software.amazon.ionschema.internal

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation

internal class TypeNamed(
        ion: IonSymbol,
        internal val type: TypeInternal
) : TypeInternal by type, ConstraintBase(ion) {

    override fun validate(value: IonValue, issues: Violations) {
        val struct = ion.system.newEmptyStruct()
        struct.put("type", ion.clone())
        val violation = Violation(struct, "type_mismatch")
        (type as ConstraintInternal).validate(value, violation)
        if (!violation.isValid()) {
            violation.message = "expected type %s".format(name())
            issues.add(violation)
        }
    }

    override fun name() = (ion as IonSymbol).stringValue()
}
