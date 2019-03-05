package software.amazon.ionschema.internal

import software.amazon.ion.IonList
import software.amazon.ion.IonString
import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type

internal class SchemaImpl(
        private val schemaSystem: IonSchemaSystem,
        private val schemaCore: SchemaCore,
        schemaContent: Iterator<IonValue>
) : Schema {

    private val types: Map<String, Type>
    private val deferredTypeReferences = mutableListOf<TypeReferenceDeferred>()

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

        resolveDeferredTypeReferences()
    }

    private fun loadHeader(typeMap: MutableMap<String, Type>, header: IonStruct) {
        (header.get("imports") as? IonList)?.forEach {
            if (it is IonStruct) {
                val id = it.get("id") as IonString
                val importedSchema = schemaSystem.loadSchema(id.stringValue())

                val typeName = it.get("type") as? IonSymbol
                if (typeName != null) {
                    var newTypeName = typeName.stringValue()
                    val newType = importedSchema.getType(newTypeName)
                    newType ?: throw InvalidSchemaException(
                                "Schema $id doesn't contain a type named '$newTypeName'")
                    val alias = it["as"] as? IonSymbol
                    if (alias != null) {
                        newTypeName = alias.stringValue()
                    }
                    addType(typeMap, newTypeName, newType)
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

    override fun newType(isl: IonStruct): Type {
        val type = TypeImpl(isl, this)
        resolveDeferredTypeReferences()
        return type
    }

    override fun getSchemaSystem() = schemaSystem

    internal fun addDeferredType(typeRef: TypeReferenceDeferred) {
        deferredTypeReferences.add(typeRef)
    }

    private fun resolveDeferredTypeReferences() {
        do {
            var resolvedSomething = false
            val iter = deferredTypeReferences.listIterator()
            while (iter.hasNext()) {
                val it = iter.next()
                if (it.attemptToResolve()) {
                    iter.remove()
                    resolvedSomething = true
                }
            }
        } while (resolvedSomething)

        if (deferredTypeReferences.size > 0) {
            throw InvalidSchemaException("Unable to resolve type reference(s): $deferredTypeReferences")
        }
    }
}

