package software.amazon.ionschema.internal

import software.amazon.ion.*
import software.amazon.ionschema.*

internal class SchemaImpl(
        private val schemaSystem: IonSchemaSystem,
        private val schemaCore: SchemaCore,
        schemaContent: Iterator<IonValue>
) : Schema {

    private val types: Map<String, Type>

    init {
        types = mutableMapOf()
        var foundHeader = false
        var foundFooter = false

        while (schemaContent.hasNext() && !foundFooter) {
            val it = schemaContent.next()

            if (it is IonSymbol && it.stringValue() == "\$ion_schema_1_0") {
                // TBD

            } else if (it.hasTypeAnnotation("schema_header")) {
                loadHeader(types, it as IonStruct)
                foundHeader = true

            } else if (it.hasTypeAnnotation("type") && it is IonStruct) {
                val newType = TypeImpl(it, this)
                addType(types, newType.name(), newType)
            } else if (it.hasTypeAnnotation("schema_footer")) {
                foundFooter = true
            }
        }

        if (foundHeader && !foundFooter) {
            throw InvalidSchemaException("Found a schema_header, but not a schema_footer")
        }
        if (!foundHeader && foundFooter) {
            throw InvalidSchemaException("Found a schema_footer, but not a schema_header")
        }
    }

    private fun loadHeader(typeMap: MutableMap<String, Type>, header: IonStruct) {
        (header.get("imports") as? IonList)?.forEach {
            if (it is IonStruct) {
                val id = it.get("id") as IonString
                val importedSchema = schemaSystem.loadSchema(id.stringValue())

                val typeName = it.get("type") as? IonSymbol
                if (typeName != null) {
                    val newType = importedSchema.getType(typeName.stringValue())
                    if (newType != null) {
                        var newTypeName = typeName
                        val alias = it.get("as") as? IonSymbol
                        if (alias != null) {
                            newTypeName = alias
                        }
                        addType(typeMap, newTypeName.stringValue(), newType)
                    }
                } else {
                    importedSchema.getTypes().forEach {
                        addType(typeMap, it.name(), it)
                    }
                }
            }
        }
    }

    private fun addType(typeMap: MutableMap<String, Type>, name: String, type: Type) {
        if (getType(name) != null) {
            throw InvalidSchemaException("Duplicate type name/alias encountered:  '$name'")
        }
        typeMap[name] = type
    }

    override fun getType(name: String) = schemaCore.getType(name) ?: types[name]

    override fun getTypes(): Iterator<Type> =
            (schemaCore.getTypes().asSequence() + types.values.asSequence())
                    .filter { it is TypeNamed || it is TypeImpl }
                    .iterator()

    override fun newType(isl: String) = newType(
            (schemaSystem as IonSchemaSystemImpl).getIonSystem().singleValue(isl) as IonStruct)

    override fun newType(isl: IonStruct) = TypeImpl(isl, this)

    override fun getSchemaSystem() = schemaSystem
}

