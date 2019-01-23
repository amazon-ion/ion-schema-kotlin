package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.*

// TBD this class doesn't provide any functionality other than type/delegate construction;
//     refactor it out?
internal class TypeReference private constructor (
        ion: IonValue,
        private var type: TypeInternal
) : TypeInternal by type, ConstraintBase(ion) {

    constructor(ion: IonValue, schema: Schema, isField: Boolean = false) : this(ion, delegate(ion, schema, isField))

    companion object {
        private fun delegate(ion: IonValue, schema: Schema, isField: Boolean): TypeInternal {
            var tmpType = when (ion) {
                is IonStruct -> {
                    val tmpIon = ion.cloneAndRemove("occurs")

                    val id = tmpIon.get("id") as? IonText
                    if (id != null) {
                        // import
                        val newSchema = schema.getSchemaSystem().loadSchema(id.stringValue())
                        val typeName = tmpIon.get("type") as IonSymbol
                        newSchema.getType(typeName)

                    } else {
                        if (isField) {
                            TypeImpl(tmpIon, schema)
                        } else {
                            if (tmpIon.size() == 1 && tmpIon.get("type") != null) {
                                // elide inline types defined as "{ type: X }" to TypeImpl;
                                // this avoids creating a nested, redundant validation structure
                                TypeImpl(tmpIon, schema)
                            } else {
                                TypeInline(tmpIon, schema)
                            }
                        }
                    }
                }

                is IonSymbol -> {
                    val t = schema.getType(ion) as TypeInternal
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

    override fun name() = type.name()
}

