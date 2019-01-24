package software.amazon.ionschema.internal

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation

internal class TypeIon(
        private val name: IonSymbol
    ) : TypeInternal, ConstraintBase(name), TypeBuiltin {

    private val ionType = IonType.valueOf(name.stringValue().toUpperCase().substring(1))

    override fun name() = name.stringValue()

    override fun isValidForBaseType(value: IonValue) = ionType.equals(value.type)

    override fun validate(value: IonValue, issues: Violations) {
        if (!ionType.equals(value.type)) {
            issues.add(Violation(ion, "type_mismatch",
                    "expected type %s, found %s".format(
                            ionType.toString().toLowerCase(),
                            value.type.toString().toLowerCase())))
        }
    }
}
