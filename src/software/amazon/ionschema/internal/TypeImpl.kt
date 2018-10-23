package software.amazon.ionschema.internal

import software.amazon.ion.*
import software.amazon.ionschema.Constraint
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.constraint.TypeReference

internal class TypeImpl(
        private val ion: IonStruct,
        private val schema: Schema,
        addDefaultTypeConstraint: Boolean = true
    ) : TypeInternal {

    private companion object {
        private val ANY = ION.newSymbol("any")
    }

    private val constraints: List<Constraint>

    init {
        var foundTypeConstraint = false
        val tmpConstraints = ion
                .filter { it.fieldName == null || schema.getSchemaSystem().isConstraint(it.fieldName) }
                .map {
                    if (it.fieldName.equals("type")) {
                        foundTypeConstraint = true
                    }
                    schema.getSchemaSystem().constraintFor(it, schema, this)
                }
                .toMutableList()

        if (!foundTypeConstraint && addDefaultTypeConstraint) {
            // default type is 'any':
            tmpConstraints.add(TypeReference(ANY, schema))
        }

        constraints = tmpConstraints.toList()
    }

    override fun name() = ion.get("name") as IonSymbol

    override fun isValid(value: IonValue): Boolean {
        constraints.forEach {
            if (!it.isValid(value)) {
                return false
            }
        }
        return true
    }

    override fun isValidForBaseType(value: IonValue): Boolean {
        val type = ion.get("type")
        if (type != null && type is IonText) {
            val baseTypeName = type.stringValue()
            val baseType = schema.getType(baseTypeName)
            if (baseType != null) {
                return (baseType as TypeInternal).isValidForBaseType(value)
            }
        }
        return (schema.getType("any")!! as TypeInternal).isValidForBaseType(value)
    }
}
