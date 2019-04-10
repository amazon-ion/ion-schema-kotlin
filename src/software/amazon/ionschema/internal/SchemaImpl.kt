package software.amazon.ionschema.internal

import software.amazon.ion.IonList
import software.amazon.ion.IonString
import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonValue
import software.amazon.ionschema.EMPTY_ITERATOR
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type

/**
 * Implementation of [Schema] for all user-provided ISL.
 */
internal class SchemaImpl internal constructor(
        private val schemaSystem: IonSchemaSystemImpl,
        private val schemaCore: SchemaCore,
        schemaContent: Iterator<IonValue>,
        /*
         * [types] is declared as a MutableMap in order to be populated DURING
         * INITIALIZATION ONLY.  This enables type B to find its already-loaded
         * dependency type A.  After initialization, [types] is expected to
         * be treated as immutable as required by the Schema interface.
         */
        private val types: MutableMap<String, Type> = mutableMapOf()
) : Schema {

    private val deferredTypeReferences = mutableListOf<TypeReferenceDeferred>()

    private constructor(
            schemaSystem: IonSchemaSystemImpl,
            schemaCore: SchemaCore,
            types: MutableMap<String, Type>
        ) : this(schemaSystem, schemaCore, EMPTY_ITERATOR, types)

    init {
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

    override fun plusType(type: Type): Schema {
        val newTypes = types.toMutableMap()
        newTypes[type.name] = type
        return SchemaImpl(schemaSystem, schemaCore, newTypes)
    }

    override fun getSchemaSystem() = schemaSystem

    internal fun addDeferredType(typeRef: TypeReferenceDeferred) {
        deferredTypeReferences.add(typeRef)
    }

    private fun resolveDeferredTypeReferences() {
        val unresolvedDeferredTypeReferences = deferredTypeReferences
                .filterNot { it.attemptToResolve() }
                .map { it.name }.toSet()

        if (unresolvedDeferredTypeReferences.isNotEmpty()) {
            throw InvalidSchemaException(
                    "Unable to resolve type reference(s): $unresolvedDeferredTypeReferences")
        }
    }
}

