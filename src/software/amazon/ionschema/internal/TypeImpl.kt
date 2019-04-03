package software.amazon.ionschema.internal

import software.amazon.ion.*
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations

/**
 * Implementation of [Type] backed by a collection of zero or more [Constraint]s.
 *
 * If no "type" constraint is found, the default "type: any" is inserted by this class
 * (unless addDefaultTypeConstraint is `false`).
 */
internal class TypeImpl(
        private val ionStruct: IonStruct,
        private val schema: Schema,
        addDefaultTypeConstraint: Boolean = true
    ) : TypeInternal, ConstraintBase(ionStruct) {

    private companion object {
        private val ION = IonSystemBuilder.standard().build()
        private val ANY = ION.newSymbol("any")
    }

    internal val constraints: List<Constraint>

    init {
        var foundTypeConstraint = false
        constraints = ionStruct.asSequence()
                .filter { it.fieldName == null || (schema.getSchemaSystem() as IonSchemaSystemImpl).isConstraint(it.fieldName) }
                .map {
                    if (it.fieldName.equals("type")) {
                        foundTypeConstraint = true
                    }
                    (schema.getSchemaSystem() as IonSchemaSystemImpl).constraintFor(it, schema)
                }
                .toMutableList()

        if (!foundTypeConstraint && addDefaultTypeConstraint) {
            // default type is 'any':
            constraints.add(TypeReference.create(ANY, schema)())
        }
    }

    override val name = (ionStruct.get("name") as? IonSymbol)?.stringValue() ?: ionStruct.toString()

    override fun getBaseType(): TypeBuiltin {
        val type = ionStruct["type"]
        if (type != null && type is IonSymbol) {
            val parentType = schema.getType(type.stringValue())
            if (parentType != null) {
                return (parentType as TypeInternal).getBaseType()
            }
        }
        return schema.getType("any")!! as TypeBuiltin
    }

    override fun isValidForBaseType(value: IonValue) = getBaseType().isValidForBaseType(value)

    override fun validate(value: IonValue, issues: Violations) {
        constraints.forEach {
            (it as ConstraintInternal).validate(value, issues)
        }
    }
}
