package software.amazon.ionschema.internal

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.internal.util.Violations
import software.amazon.ionschema.internal.util.Violation
import software.amazon.ionschema.internal.util.CommonViolations

internal class TypeCore(
        private val name: IonSymbol
    ) : TypeInternal, ConstraintBase(name), TypeBuiltin {

    private val ionType = IonType.valueOf(name.stringValue().toUpperCase())

    override fun name() = name.stringValue()

    override fun isValidForBaseType(value: IonValue) = ionType.equals(value.type)

    override fun validate(value: IonValue, issues: Violations) {
        if (!ionType.equals(value.type)) {
            issues.add(Violation(ion, "type_mismatch",
                    "expected type %s, found %s".format(
                            ionType.toString().toLowerCase(),
                            value.type.toString().toLowerCase())))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        }
    }
}
