package software.amazon.ionschema.internal

import software.amazon.ion.*
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.Violations

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
        val tmpConstraints = ionStruct
                .filter { it.fieldName == null || (schema.getSchemaSystem() as IonSchemaSystemImpl).isConstraint(it.fieldName) }
                .map {
                    if (it.fieldName.equals("type")) {
                        foundTypeConstraint = true
                    }
                    (schema.getSchemaSystem() as IonSchemaSystemImpl).constraintFor(it, schema, this)
                }
                .toMutableList()

        if (!foundTypeConstraint && addDefaultTypeConstraint) {
            // default type is 'any':
            tmpConstraints.add(TypeReference.create(ANY, schema)())
        }

        constraints = tmpConstraints.toList()
    }

    override fun name() = (ionStruct.get("name") as? IonSymbol)?.stringValue() ?: ionStruct.toString()

    override fun getBaseType(): TypeBuiltin {
        val type = ionStruct.get("type")
        type?.let {
            if (type is IonSymbol) {
                val parentType = schema.getType(type.stringValue())
                parentType?.let {
                    return (parentType as TypeInternal).getBaseType()
                }
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
