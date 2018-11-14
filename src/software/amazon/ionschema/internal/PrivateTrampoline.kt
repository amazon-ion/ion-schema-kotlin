package software.amazon.ionschema.internal;

import software.amazon.ion.IonStruct
import software.amazon.ion.IonSystem
import software.amazon.ion.IonValue
import software.amazon.ionschema.Authority;
import software.amazon.ionschema.IonSchemaSystem
import software.amazon.ionschema.Schema
import software.amazon.ionschema.Type

class PrivateTrampoline {
    companion object {
        fun newIonSchemaSystem(ionSystem: IonSystem, authorities: List<Authority>): IonSchemaSystem
                = IonSchemaSystemImpl(ionSystem, authorities)

        fun newSchemaImpl(schemaSystem: IonSchemaSystem, schema: Schema, iter: Iterator<IonValue>): Schema
                = SchemaImpl(schemaSystem, schema as SchemaCore, iter)

        fun newTypeImpl(ion: IonValue, schemaCore: Schema): Type
                = TypeImpl(ion as IonStruct, schemaCore)

        fun newSchemaCore(schemaSystem: IonSchemaSystem): Schema
                = SchemaCore(schemaSystem)
    }
}