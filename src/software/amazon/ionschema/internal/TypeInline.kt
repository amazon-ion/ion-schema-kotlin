package software.amazon.ionschema.internal

import software.amazon.ion.IonStruct
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Violations
import software.amazon.ionschema.Violation
import software.amazon.ionschema.internal.constraint.ConstraintBase

internal class TypeInline private constructor (
        override val ion: IonStruct,
        private val type: TypeInternal
) : ConstraintBase(ion), TypeInternal by type {

    constructor(ionStruct: IonStruct, schema: Schema)
            : this(ionStruct, TypeImpl(ionStruct, schema))

    override fun name() = type.name()

    override fun validate(value: IonValue, issues: Violations) {
        val violation = Violation(ion, "type_mismatch")
        (type as ConstraintInternal).validate(value, violation)
        if (!violation.isValid()) {
            violation.message = "expected type %s".format(name())
            issues.add(violation)
        }
    }
}

