package software.amazon.ionschema.internal

import software.amazon.ion.IonStruct
import software.amazon.ion.IonValue
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.util.Violations
import software.amazon.ionschema.internal.util.Violation

internal class TypeInline private constructor (
        private val ionStruct: IonStruct,
        private val type: TypeInternal
) : TypeInternal by type {

    constructor(ionStruct: IonStruct, schema: Schema)
            : this(ionStruct, TypeImpl(ionStruct, schema))

    override fun validate(value: IonValue, issues: Violations) {
        val violation = Violation(ionStruct, "type_mismatch")
        type.validate(value, violation)
        if (!violation.isValid()) {
            violation.message = "expected type %s".format(name())
            issues.add(violation)
        }
    }
}
