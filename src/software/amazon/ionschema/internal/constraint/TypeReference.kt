package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.SchemaImpl
import software.amazon.ionschema.internal.TypeBuiltin
import software.amazon.ionschema.internal.TypeImpl
import software.amazon.ionschema.internal.TypeInline
import software.amazon.ionschema.internal.TypeInternal
import software.amazon.ionschema.internal.TypeNamed
import software.amazon.ionschema.internal.TypeNullable

internal class TypeReference private constructor() {
    companion object {
        fun create(ion: IonValue, schema: Schema, isField: Boolean = false): () -> TypeInternal {
            var type = when (ion) {
                is IonStruct -> {
                    val id = ion.get("id") as? IonText
                    if (id != null) {
                        // import
                        val newSchema = schema.getSchemaSystem().loadSchema(id.stringValue())
                        val typeName = ion.get("type") as IonSymbol
                        newSchema.getType(typeName.stringValue())

                    } else {
                        if (isField) {
                            TypeImpl(ion, schema)
                        } else {
                            if (ion.size() == 1 && ion.get("type") != null) {
                                // elide inline types defined as "{ type: X }" to TypeImpl;
                                // this avoids creating a nested, redundant validation structure
                                TypeImpl(ion, schema)
                            } else {
                                TypeInline(ion, schema)
                            }
                        }
                    }
                }

                is IonSymbol -> {
                    val t = schema.getType(ion.stringValue())
                    if (t != null) {
                        if (t is TypeBuiltin) {
                            t
                        } else {
                            TypeNamed(ion, t as TypeInternal)
                        }
                    } else {
                        // type can't be resolved yet;  ask the schema to try again later
                        val deferredType = TypeReferenceDeferred(ion, schema)
                        (schema as SchemaImpl).addDeferredType(deferredType)
                        return { deferredType.resolve() }
                    }
                }

                else -> throw InvalidSchemaException("Unable to resolve type reference '${ion}'")
            }

            if (type == null) {
                throw InvalidSchemaException("Unable to resolve type reference '${ion}'")
            }

            if (ion.hasTypeAnnotation("nullable")) {
                type = TypeNullable(ion, type as TypeInternal, schema)
            }
            return { type as TypeInternal }
        }
    }
}

internal class TypeReferenceDeferred(
        private val ion: IonSymbol,
        private val schema: Schema
) {

    private var type: TypeInternal? = null

    fun attemptToResolve(): Boolean {
        type = schema.getType(ion.stringValue()) as? TypeInternal
        return type != null
    }

    fun resolve(): TypeInternal = type!!
}

