package software.amazon.ionschema.internal

import software.amazon.ion.IonStruct
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Violation
import software.amazon.ionschema.Violations
import software.amazon.ionschema.internal.constraint.ConstraintBase

/**
 * Type implementation instantiated to represent built-in types.
 *
 * @see TypeBuiltin
 */
internal class TypeBuiltinImpl private constructor(
        ion : IonStruct,
        private val delegate: TypeInternal
) : TypeInternal by delegate, ConstraintBase(ion), TypeBuiltin {

    constructor (ionStruct: IonStruct, schema: Schema)
            : this(ionStruct, TypeImpl(ionStruct, schema, addDefaultTypeConstraint = false))

    override val name = ion.fieldName

    override fun validate(value: IonValue, issues: Violations) {
        val struct = ion.system.newEmptyStruct()
        struct.put("type", ion.system.newSymbol(name))
        val violation = Violation(struct, "type_mismatch")
        delegate.validate(value, violation)
        if (!violation.isValid()) {
            violation.message = "expected type %s".format(name)
            issues.add(violation)
        }
    }
}

