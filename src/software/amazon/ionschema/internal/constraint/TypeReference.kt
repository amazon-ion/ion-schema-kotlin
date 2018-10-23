package software.amazon.ionschema.internal.constraint

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSymbol
import software.amazon.ion.IonText
import software.amazon.ion.IonValue
import software.amazon.ionschema.InvalidSchemaException
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema
import software.amazon.ionschema.internal.TypeImpl
import software.amazon.ionschema.internal.TypeInternal

internal class TypeReference(
        ion: IonValue,
        schema: Schema
    ) : ConstraintBase(ion) {

    private val type: TypeInternal
    private val nullable: Boolean

    init {
        val tmpType = when (ion) {
                is IonStruct -> {
                    val tmpIon = ion.clone()
                    tmpIon.remove("occurs")   // TBD

                    val id = tmpIon.get("id") as? IonText
                    if (id != null) {
                        // import
                        val newSchema = schema.getSchemaSystem().loadSchema(id.stringValue())
                        val typeName = tmpIon.get("type") as IonSymbol
                        newSchema.getType(typeName)

                    } else {
                        // inline type
                        TypeImpl(tmpIon, schema)
                    }
                }

                is IonSymbol -> schema.getType(ion)

                else -> throw InvalidSchemaException("Unable to resolve type reference '${ion}'")
            }

        if (tmpType == null) {
            throw InvalidSchemaException("Unable to resolve type reference '${ion}'")
        }

        type = tmpType as TypeInternal
        nullable = ion.hasTypeAnnotation("nullable")
    }

    //override fun name() = type.name().stringValue()

    override fun isValid(value: IonValue) =
        if (nullable && value.isNullValue
                && (value.type.equals(software.amazon.ion.IonType.NULL)
                        || type.isValidForBaseType(value))) {
            true
        } else {
            type.isValid(value)
        }
}
