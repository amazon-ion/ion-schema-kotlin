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

/**
 * Implementation of [Schema] for all user-provided ISL.
 */
internal class SchemaImpl(
        private val schemaSystem: IonSchemaSystemImpl,
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
                // TBD https://github.com/amzn/ion-schema-kotlin/issues/95

            } else if (it.hasTypeAnnotation("schema_header")) {
                loadHeader(types, it as IonStruct)
                foundHeader = true

            } else if (it.hasTypeAnnotation("type") && it is IonStruct) {
                val newType = TypeImpl(it, this)
                addType(types, newType.name, newType)
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
        (header.get("imports") as? IonList)
            ?.filterIsInstance<IonStruct>()
            ?.forEach {
                val id = it["id"] as IonString
                val importedSchema = schemaSystem.loadSchema(id.stringValue())

                val typeName = (it["type"] as? IonSymbol)?.stringValue()
                if (typeName != null) {
                    val newType = importedSchema.getType(typeName)
                            ?: throw InvalidSchemaException(
                                "Schema $id doesn't contain a type named '$typeName'")

                    val alias = it["as"] as? IonSymbol
                    val newTypeName = alias?.stringValue() ?: typeName
                    addType(typeMap, newTypeName, newType)
                } else {
                    importedSchema.getTypes().forEach {
                        addType(typeMap, it.name, it)
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
            schemaSystem.getIonSystem().singleValue(isl) as IonStruct)

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
        val unresolvedDeferredTypeReferences = deferredTypeReferences.filter {
            !it.attemptToResolve()
        }.map { it.name }.toSet()

        if (unresolvedDeferredTypeReferences.isNotEmpty()) {
            throw InvalidSchemaException(
                    "Unable to resolve type reference(s): $unresolvedDeferredTypeReferences")
        }
    }
}

