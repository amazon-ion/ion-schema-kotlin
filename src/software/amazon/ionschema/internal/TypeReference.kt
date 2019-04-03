package software.amazon.ionschema.internal

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Violations

/**
 * Provides a factory method that translates an ISL type reference into a function
 * that returns a Type instance.
 *
 * Types that can't be resolved yet are instantiated as [TypeReferenceDeferred] objects
 * that are resolved by [SchemaImpl.resolveDeferredTypeReferences] prior to asserting
 * that the schema is valid.
 */
internal class TypeReference private constructor() {
    companion object {
        fun create(ion: IonValue, schema: Schema, isField: Boolean = false): () -> TypeInternal {
            if (ion.isNullValue) {
                throw InvalidSchemaException("Unable to resolve type reference '$ion'")
            }

            return when (ion) {
                is IonStruct -> handleStruct(ion, schema, isField)
                is IonSymbol -> handleSymbol(ion, schema)
                else -> throw InvalidSchemaException("Unable to resolve type reference '$ion'")
            }
        }

        private fun handleStruct(ion: IonStruct, schema: Schema, isField: Boolean): () -> TypeInternal {
            val id = ion["id"] as? IonText
            val type = when {
                id != null -> {
                    // import
                    val newSchema = schema.getSchemaSystem().loadSchema(id.stringValue())
                    val typeName = ion.get("type") as IonSymbol
                    newSchema.getType(typeName.stringValue()) as? TypeInternal
                }
                isField -> TypeImpl(ion, schema)
                ion.size() == 1 && ion["type"] != null -> {
                    // elide inline types defined as "{ type: X }" to TypeImpl;
                    // this avoids creating a nested, redundant validation structure
                    TypeImpl(ion, schema)
                }
                else -> TypeInline(ion, schema)
            }

            type ?: throw InvalidSchemaException("Unable to resolve type reference '$ion'")

            val theType = handleNullable(ion, schema, type)
            return { theType }
        }

        private fun handleSymbol(ion: IonSymbol, schema: Schema): () -> TypeInternal {
            val t = schema.getType(ion.stringValue())
            return if (t != null) {
                    val type = t as? TypeBuiltin ?: TypeNamed(ion, t as TypeInternal)
                    val theType = handleNullable(ion, schema, type);
                    { theType }
                } else {
                    // type can't be resolved yet;  ask the schema to try again later
                    val deferredType = TypeReferenceDeferred(ion, schema)
                    (schema as SchemaImpl).addDeferredType(deferredType);
                    { deferredType.resolve() }
                }
        }

        private fun handleNullable(ion: IonValue, schema: Schema, type: TypeInternal): TypeInternal =
            if (ion.hasTypeAnnotation("nullable")) {
                TypeNullable(ion, type, schema)
            } else {
                type
            }
    }
}

/**
 * Represents a type reference that can't be resolved yet.
 */
internal class TypeReferenceDeferred(
        ion: IonSymbol,
        private val schema: Schema
) : TypeInternal {

    private var type: TypeInternal? = null
    override val name: String = ion.stringValue()

    fun attemptToResolve(): Boolean {
        type = schema.getType(name) as? TypeInternal
        return type != null
    }

    fun resolve(): TypeInternal = type!!

    override fun getBaseType(): TypeBuiltin = throw UnsupportedOperationException()

    override fun isValidForBaseType(value: IonValue): Boolean = throw UnsupportedOperationException()

    override fun validate(value: IonValue, issues: Violations) = throw UnsupportedOperationException()
}

