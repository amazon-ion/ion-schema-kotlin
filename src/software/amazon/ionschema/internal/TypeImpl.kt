package software.amazon.ionschema.internal

import software.amazon.ion.*
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.Constraint
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.ConstraintBase
import software.amazon.ionschema.internal.constraint.TypeReference
import software.amazon.ionschema.internal.util.Violations

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
            tmpConstraints.add(TypeReference(ANY, schema))
        }

        constraints = tmpConstraints.toList()
    }

    override fun name() = (ionStruct.get("name") as? IonSymbol)?.stringValue() ?: ionStruct.toString()

    override fun isValidForBaseType(value: IonValue): Boolean {
        val type = ionStruct.get("type")
        if (type != null && type is IonText) {
            val baseTypeName = type.stringValue()
            val baseType = schema.getType(baseTypeName)
            if (baseType != null) {
                return (baseType as TypeInternal).isValidForBaseType(value)
            }
        }
        return (schema.getType("any")!! as TypeInternal).isValidForBaseType(value)
    }

    override fun validate(value: IonValue, issues: Violations) {
        constraints.forEach {
            it.validate(value, issues)
        }
    }
}
