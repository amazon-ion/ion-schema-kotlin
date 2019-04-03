package software.amazon.ionschema.internal

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation

/**
 * Instantiated to represent individual Ion Types as defined by the
 * Ion Schema Specification.
 */
internal class TypeIon(
        nameSymbol: IonSymbol
    ) : TypeInternal, ConstraintBase(nameSymbol), TypeBuiltin {

    private val ionType = IonType.valueOf(nameSymbol.stringValue().toUpperCase().substring(1))

    override val name = nameSymbol.stringValue()

    override fun getBaseType() = this

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
