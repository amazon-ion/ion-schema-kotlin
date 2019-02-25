package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.*

internal class TypeReference private constructor() {
    companion object {
        fun create(ion: IonValue, schema: Schema, isField: Boolean = false): TypeInternal {
            var tmpType = when (ion) {
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
                    val t = schema.getType(ion.stringValue()) as TypeInternal
                    if (t is TypeBuiltin) {
                        t
                    } else {
                        TypeNamed(ion, t)
                    }
                }

                else -> throw InvalidSchemaException("Unable to resolve type reference '${ion}'")
            }

            if (tmpType == null) {
                throw InvalidSchemaException("Unable to resolve type reference '${ion}'")
            }

            if (ion.hasTypeAnnotation("nullable")) {
                tmpType = TypeNullable(ion, tmpType as TypeInternal)
            }
            return tmpType as TypeInternal
        }
    }
}

