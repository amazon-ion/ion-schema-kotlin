package software.amazon.ionschema.internal

import software.amazon.ion.IonSymbol
import software.amazon.ion.IonType
import software.amazon.ion.IonValue
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.CommonViolations

internal class TypeCore(
        name: IonSymbol
    ) : TypeInternal, ConstraintBase(name), TypeBuiltin {

    private val ionType: IonType
    private val ionTypeName: String

    init {
        ionType = when (name.stringValue().toUpperCase()) {
                "DOCUMENT" -> IonType.DATAGRAM
                else -> IonType.valueOf(name.stringValue().toUpperCase())
            }
        ionTypeName = ionType.schemaTypeName()
    }

    override fun name() = ionTypeName

    override fun getBaseType() = this

    override fun isValidForBaseType(value: IonValue) = ionType.equals(value.type)

    override fun validate(value: IonValue, issues: Violations) {
        if (!ionType.equals(value.type)) {
            issues.add(Violation(ion, "type_mismatch",
                    "expected type %s, found %s".format(
                            ionTypeName,
                            value.type.schemaTypeName())))
        } else if (value.isNullValue) {
            issues.add(CommonViolations.NULL_VALUE(ion))
        }
    }
}

internal fun IonType.schemaTypeName() = when (this) {
        IonType.DATAGRAM -> "document"
        else -> this.toString().toLowerCase()
    }

